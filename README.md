# Otomasi Jadwal — Backend

Backend REST API untuk sistem **Otomasi Penjadwalan Perkuliahan**: pengelolaan data akademik (mata kuliah, dosen, ruangan, lab, program studi) hingga proses generate jadwal kuliah secara otomatis. Dibangun dengan **Java 17** dan **Spring Boot 3.3.1**.

---

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Tech Stack](#tech-stack)
- [Struktur Proyek](#struktur-proyek)
- [Arsitektur & Catatan Penting](#arsitektur--catatan-penting)
- [Daftar Endpoint API](#daftar-endpoint-api)
- [Prasyarat](#prasyarat)
- [Konfigurasi Environment](#konfigurasi-environment)
- [Cara Menjalankan](#cara-menjalankan)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Roadmap](#roadmap)

---

## Fitur Utama

- **Master data akademik**: Kategori mata kuliah, Mata Kuliah, Ruangan, Lab, Dosen, Spesialisasi, Program Studi (Prodi/SubMajor), dan User (admin).
- **Autentikasi berbasis session token**: login menghasilkan token yang disimpan di database, dan dikirim ulang oleh client lewat header `Token` pada setiap request yang membutuhkan login.
- **Generate jadwal otomatis (Timeline Generation)**: proses generate dipicu oleh admin, kemudian dieksekusi oleh service komputasi terpisah (lihat [Arsitektur & Catatan Penting](#arsitektur--catatan-penting)).
- **Realtime status generate** lewat WebSocket, sehingga client tahu kapan proses generate sedang berjalan/selesai tanpa perlu polling.
- **Penyajian gambar** (foto dosen, dsb.) lewat endpoint dedicated berbasis path ter-enkode Base64.
- **Push notification** via Firebase Cloud Messaging (opsional).
- **Dokumentasi API otomatis** lewat Swagger/OpenAPI.

---

## Tech Stack

| Komponen | Teknologi |
|---|---|
| Bahasa & Runtime | Java 17 |
| Framework | Spring Boot 3.3.1 |
| Build tool | Maven (tersedia Maven Wrapper `mvnw` / `mvnw.cmd`) |
| Database | MariaDB 10.11 — driver `mariadb-java-client` |
| ORM | Spring Data JPA / Hibernate |
| Database testing | H2 (in-memory) |
| Security layer | Spring Security (CORS only — lihat [Autentikasi](#autentikasi-token-custom-bukan-jwt)) |
| Realtime | Spring WebSocket (`TextWebSocketHandler`) |
| Dokumentasi API | springdoc-openapi (Swagger UI) |
| Push Notification | Firebase Admin SDK |
| Email | Spring Mail (SMTP) |
| Image processing | Thumbnailator |
| Data dummy/testing | JavaFaker |
| Monitoring | Spring Boot Actuator |
| Container | Docker (multi-stage build) + Docker Compose |
| CI/CD | Jenkins (`Jenkinsfile`) |

---

## Struktur Proyek

```
Otomasi-Jadwal-Backend/
├── src/main/java/com/jadwal/restfulapi/
│   ├── JadwalApplication.java       # Entry point
│   ├── controller/                  # REST controller
│   ├── service/                     # Business logic
│   ├── repository/                  # Spring Data JPA repository
│   ├── model/                       # Entity JPA (Course, Lecturer, Room, Schedule, dst.)
│   ├── model/enums/                 # Enum: Day, Role, Religion
│   ├── dto/                         # Request/response DTO
│   ├── config/                      # Security, CORS, Swagger, WebSocket, Firebase, Web MVC
│   ├── interceptor/                 # Interceptor global (lock saat proses generate)
│   ├── handler/                     # WebSocket handler (broadcast status generate)
│   ├── annotation/                  # Anotasi custom untuk dokumentasi Swagger
│   ├── seeder/                      # Auto-seed data awal (slot jadwal, status generate)
│   └── util/                        # Helper (hash password, kode HTTP, error message)
├── src/main/resources/
│   └── application.properties       # Konfigurasi utama (dibaca dari environment variable)
├── src/test/                        # Unit test, feature test, integration test
├── .env.example                     # Contoh variabel environment
├── Dockerfile                       # Multi-stage build (Maven builder → JRE Alpine runtime)
├── docker-compose.deploy.yml        # Compose untuk deployment (app + MariaDB + phpMyAdmin)
├── jenkins-docker-compose.yaml      # Compose untuk menjalankan server Jenkins
├── Jenkinsfile                      # Pipeline CI/CD
├── mvnw / mvnw.cmd                  # Maven wrapper
└── pom.xml                          # Dependensi & konfigurasi build Maven
```

---

## Arsitektur & Catatan Penting

### Autentikasi: token custom, bukan JWT

- Spring Security (`SecurityConfig`) hanya dipakai untuk konfigurasi **CORS**; semua request pada level filter Spring Security diizinkan lewat (`permitAll`).
- Autentikasi dilakukan **manual di setiap method controller**: controller membaca header `Token`, lalu memvalidasinya lewat `AuthService.findSessionBySessionToken(token)` terhadap tabel `Session`. Token tidak valid/kadaluarsa akan menghasilkan `401 Unauthorized`.
- Anotasi `@NoAuth` pada controller **hanya memengaruhi tampilan Swagger UI** (menandai endpoint mana yang tidak butuh gembok Token di dokumentasi) — anotasi ini tidak melakukan pemblokiran otomatis apa pun; validasi token tetap ditulis manual per endpoint.
- Session kadaluarsa dibersihkan otomatis setiap 5 detik lewat scheduled task (`SchedulingService`).
- Role yang tersedia (`Role` enum): `PRODI` (Prodi Admin), `BAA` (BAA Admin), `PM` (PM Admin), `NTHUM` (NTHUM Admin), `SUPERADMIN` (Super Admin). Otorisasi per-role dicek manual di service/controller terkait.

### Generate jadwal didelegasikan ke service komputasi terpisah

Proses generate jadwal **tidak dihitung di dalam backend Java ini**. Backend hanya berperan sebagai *pemanggil* (trigger) — komputasi algoritma penjadwalan sengaja dijalankan di proses/service terpisah agar tidak membebani proses backend utama.

Alurnya:

1. Admin (`SUPERADMIN`/`BAA`/`PRODI`) memanggil `POST /api/v1/timeline/generate` dengan body `{ "academicYear": 2026, "isOdd": true }` beserta header `Token`.
2. Backend menandai status `isGenerating = true`, membuat `secretKey`, lalu memanggil secara **asynchronous (fire-and-forget)** service eksternal di:
   ```
   http://startgenerate-service:8082/startGenerate
   ```
   Service ini dikelola dan di-deploy secara terpisah dari repository ini, dan harus dapat diakses dari jaringan yang sama dengan backend (mis. Docker network yang sama, dengan nama host `startgenerate-service`).
3. Selama proses generate berjalan, `RequestInterceptor` memblokir semua request tulis (`POST`/`PUT`/`PATCH`/`DELETE`) selain ke `AuthController`, `UserController`, dan `timeline/generateComplete` — response `409 Conflict`.
4. Setelah selesai, service eksternal memanggil balik `POST /api/v1/timeline/generateComplete` dengan body berisi `secretKey` untuk menandai proses selesai dan membuka kembali lock tulis.
5. Perubahan status generate (`isGenerating`, `isOdd`, `academicYear`) di-broadcast realtime ke seluruh client yang terhubung ke WebSocket `/status`.

> Tanpa service eksternal tersebut berjalan, seluruh endpoint CRUD lain tetap berfungsi normal; hanya proses generate yang tidak akan pernah mencapai status selesai secara otomatis. Endpoint `GET /api/v1/timeline/toggleGenerate` disediakan untuk keperluan testing lokal saja dan **tidak untuk digunakan di production**.

### Firebase bersifat opsional

Bean Firebase (`FirebaseConfig`) hanya diinisialisasi apabila file kredensial JSON-nya tersedia di classpath (`src/main/resources/`). Jika file tidak ditemukan, aplikasi tetap berjalan normal dan fitur push notification otomatis nonaktif.

### Penyajian gambar

Gambar disajikan lewat `GET /api/v1/images/**`, dengan path file di-encode Base64 pada URL lalu di-decode di server untuk dibaca dari filesystem (folder `images/`, sesuai konfigurasi `storage.upload-dir`).

---

## Daftar Endpoint API

Seluruh endpoint menggunakan prefix `${storage.api-prefix}`, default-nya **`/api/v1`**.

| Modul | Base Path | Method Tersedia |
|---|---|---|
| Auth | `/api/v1/auth` | `POST /login`, `POST /logout`, `GET /check` |
| User | `/api/v1/user` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Category | `/api/v1/category` | `GET /all`, `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Course | `/api/v1/course` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `PATCH /toggle/{id}/true\|false` |
| Lecturer | `/api/v1/lecturer` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `PATCH /toggle/{id}/true\|false` |
| Room | `/api/v1/room` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Lab | `/api/v1/lab` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Specialization | `/api/v1/specialization` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Prodi | `/api/v1/prodi` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| SubMajor (BAA, lintas prodi) | `/api/v1/all/submajor` | `GET /{prodiId}`, `GET /{prodiId}/{id}`, `POST /{prodiId}`, `PUT /{prodiId}/{id}`, `DELETE /{prodiId}/{id}` |
| SubMajor (Prodi Admin) | `/api/v1/prodiAdmin/submajor` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Timeline / Generate | `/api/v1/timeline` | `GET /lectures`, `GET /schedules`, `GET /status`, `POST /generate`, `POST /generateComplete`, `GET /toggleGenerate` (testing) |
| Images | `/api/v1/images` | `GET /**` |
| WebSocket | `/status` | Broadcast status generate secara realtime |

Detail lengkap request/response tiap endpoint tersedia otomatis di Swagger UI (lihat [Cara Menjalankan](#cara-menjalankan)).

---

## Prasyarat

- **JDK 17**
- **MariaDB 10.11** (atau versi kompatibel)
- **Maven** — opsional, project sudah menyertakan Maven Wrapper (`./mvnw`)
- **Docker & Docker Compose** — opsional, untuk menjalankan lewat container
- Akun **SMTP** (mis. Gmail) untuk fitur email
- File kredensial **Firebase Admin SDK (.json)** — opsional, hanya untuk fitur push notification

---

## Konfigurasi Environment

Konfigurasi dibaca lewat environment variable yang dimuat otomatis dari file `.env` di root project (`spring.config.import=optional:file:.env[.properties]` — fitur native Spring Boot).

Terdapat dua profil environment:
- **`.env`** — untuk kebutuhan **local development**.
- **`.env.prod`** — konfigurasi **production**, digunakan pada proses deploy oleh pipeline Jenkins.

Kedua file ini masuk daftar `.gitignore` dan tidak ikut disertakan ke version control. Gunakan `.env.example` sebagai acuan:

```env
# Database Config
DB_HOST=localhost
DB_PORT=3306
DB_NAME=kuliah
DB_USER=root
DB_PASS=

# Server Config
SERVER_PORT=8080
SERVER_IMAGE_HOST=http://localhost:8080/api/v1/images

# Email Config
SMTP_USER=johndoe@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PASS=

# Firebase Config
FIREBASE_CONFIG_NAME=example-firebase-adminsdk.json
```

| Variabel | Keterangan |
|---|---|
| `DB_HOST` | Host database MariaDB (`localhost` untuk lokal, atau nama service Docker) |
| `DB_PORT` | Port MariaDB, default `3306` |
| `DB_NAME` | Nama database (dibuat otomatis jika belum ada — `createDatabaseIfNotExist=true`) |
| `DB_USER` / `DB_PASS` | Kredensial database |
| `DB_ROOT_PASS` | Password root MariaDB, dipakai saat setup container database (bukan dibaca oleh aplikasi Spring Boot) |
| `SERVER_PORT` | Port aplikasi Spring Boot |
| `SERVER_IMAGE_HOST` | Base URL publik untuk endpoint gambar |
| `SMTP_HOST` / `SMTP_USER` / `SMTP_PASS` | Kredensial SMTP |
| `FIREBASE_CONFIG_NAME` | Nama file JSON kredensial Firebase Admin SDK, diletakkan di `src/main/resources/` |

---

## Cara Menjalankan

Backend dapat dijalankan dengan tiga cara: **Maven Wrapper** (development), **build JAR manual**, atau **Docker Compose** (mendekati environment production).

### Opsi A — Development lokal dengan Maven Wrapper

1. Siapkan database MariaDB:
   ```bash
   docker run -d --name mariadb-dev -p 3306:3306 \
     -e MARIADB_ROOT_PASSWORD=root \
     -e MARIADB_DATABASE=kuliah \
     -e MARIADB_USER=root \
     -e MARIADB_PASSWORD=root \
     mariadb:10.11
   ```
2. Salin dan sesuaikan file environment:
   ```bash
   cp .env.example .env
   ```
3. *(Opsional)* Untuk fitur Firebase, letakkan file JSON kredensial di `src/main/resources/` dan set `FIREBASE_CONFIG_NAME` sesuai nama file tersebut.
4. Jalankan aplikasi dari root project:
   - Linux/macOS:
     ```bash
     ./mvnw spring-boot:run
     ```
   - Windows:
     ```cmd
     mvnw.cmd spring-boot:run
     ```
5. Log berikut menandakan aplikasi berhasil berjalan:
   ```
   Server Running On: http://localhost:8080
   Documentation On: http://localhost:8080/swagger-ui
   ```

### Opsi B — Build JAR lalu jalankan manual

```bash
./mvnw clean package -DskipTests
java -jar target/restfulapi-0.0.1-SNAPSHOT.jar
```

File `.env` harus berada di direktori kerja saat menjalankan `java -jar` (umumnya root project), karena dibaca secara relatif terhadap direktori kerja saat runtime.

### Opsi C — Docker Compose

`docker-compose.deploy.yml` menjalankan tiga service: **MariaDB**, **phpMyAdmin**, dan **backend**.

1. Build image aplikasi:
   ```bash
   docker build -t jadwal-api:0.0.1 .
   ```
2. Siapkan file `.env` berisi minimal: `DB_NAME`, `DB_USER`, `DB_PASS`, `SMTP_HOST`, `SMTP_USER`, `SMTP_PASS`, `SERVER_IMAGE_HOST`, `FIREBASE_CONFIG_NAME`.
3. Buat Docker network eksternal yang dirujuk compose file:
   ```bash
   docker network create jadwal-network
   ```
4. Jalankan seluruh service:
   ```bash
   docker compose -f docker-compose.deploy.yml up -d
   ```
5. Akses service:
   - Backend: `http://localhost:8000` (container listen di `8080`, di-mapping ke host port `8000`)
   - phpMyAdmin: `http://localhost:8081`
   - MariaDB: port `3306`

> Untuk fitur generate jadwal berfungsi penuh, service `startgenerate-service` (lihat [Arsitektur](#generate-jadwal-didelegasikan-ke-service-komputasi-terpisah)) perlu dijalankan terpisah dan tergabung dalam Docker network yang sama (`jadwal-network`), dengan nama service persis `startgenerate-service` pada port `8082`.

### Verifikasi Server

| Kebutuhan | URL |
|---|---|
| Health check | `GET http://localhost:<port>/actuator/health` |
| Swagger UI | `http://localhost:<port>/swagger-ui` |
| OpenAPI JSON | `http://localhost:<port>/v3/api-docs` |
| WebSocket status | `ws://localhost:<port>/status` |

### Autentikasi untuk Mencoba API

1. `POST /api/v1/auth/login`:
   ```json
   { "username": "namauser", "password": "passwordnya" }
   ```
   Response berisi `token` (session token).
2. Sertakan header berikut pada endpoint yang membutuhkan login:
   ```
   Token: <token-dari-login>
   ```
3. Di Swagger UI, klik **Authorize** lalu masukkan token (skema keamanan `TokenAuth`, dikirim sebagai header `Token`, bukan `Authorization: Bearer`).

> Saat ini belum tersedia endpoint registrasi maupun seeder untuk akun Super Admin pertama — lihat [Roadmap](#roadmap). Akun awal perlu dibuat langsung di database.

---

## Testing

```bash
./mvnw test
```

Cakupan test:
- **Unit test** (`dto`, `util`, `service`) — mis. `AuthServiceTest`, `CategoryServiceTest`, `RoomServiceTest`, `LoginDTOTest`.
- **Feature test** (`feature/`) — mis. `AuthControllerFeatureTest`, `CategoryControllerFeatureTest`, `RoomControllerFeatureTest`.
- **Integration test** (`integration/`) — mis. `CategoryLifecycleIntegrationTest`, `RoomLifecycleIntegrationTest`.

Seluruh test berjalan di atas database **H2 in-memory** (`src/test/resources/application.properties`), sehingga tidak memerlukan koneksi MariaDB.

---

## CI/CD

Pipeline (`Jenkinsfile`) menjalankan tahapan: **Checkout → Test (`mvn test`) → Build Docker Image → Simpan image sebagai `.tar.gz` → Transfer via SCP/SSH → Deploy dengan `docker-compose.deploy.yml`**. Pipeline ini mengasumsikan tersedianya Jenkins credential `jadwal-env-file` (isi `.env.prod`) dan `jadwal-host-ssh` (SSH key ke server deploy). File `jenkins-docker-compose.yaml` digunakan untuk menjalankan server Jenkins itu sendiri, bukan bagian dari aplikasi backend.

---

## Roadmap

- [ ] Seeder otomatis untuk membuat akun **Super Admin pertama** saat aplikasi pertama kali dijalankan.
- [ ] Dokumentasi/setup terpisah untuk service komputasi penjadwalan (`startgenerate-service`).

---

## Skema Database

Skema tabel mengikuti definisi entity JPA pada `src/main/java/com/jadwal/restfulapi/model/` (`spring.jpa.hibernate.ddl-auto=update`), tanpa file migrasi SQL terpisah. Entity utama: `Category`, `Course`, `CourseSchedule`, `CourseSpecialization`, `FreeTable`, `LabGroup`, `LabSpecialization`, `Lecture`, `LectureLecturer`, `Lecturer`, `LecturerSchedule`, `LecturerScheduleTime`, `LecturerSpecialization`, `Room`, `Schedule`, `Session`, `Specialization`, `SubMajor`, `TimelineGeneration`, `User`.
