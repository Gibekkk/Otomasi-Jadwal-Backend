package com.jadwal.restfulapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService Unit Tests")
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private MultipartFile mockMultipartFile;

    private final String HOST_PATH = "http://localhost:8080/images";
    private byte[] validImageBytes;

    @BeforeEach
    void setUp() throws IOException {
        // Suntikkan nilai @Value menggunakan ReflectionTestUtils
        ReflectionTestUtils.setField(imageService, "serverImagePath", HOST_PATH);

        // Buat dummy image 1x1 pixel dalam format byte array agar ImageIO.read() tidak mengembalikan null
        BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(tinyImage, "jpeg", baos);
        validImageBytes = baos.toByteArray();
    }

    // =====================================================================
    // saveImage() & saveFile()
    // =====================================================================
    @Nested
    @DisplayName("saveImage() & saveFile() Tests")
    class SaveImageTests {

        @Test
        @DisplayName("Returns empty string when input file is not a valid image")
        void whenInvalidImageInput_thenReturnsEmptyString(@TempDir Path tempDir) throws IOException {
            // Setup mock file mengembalikan stream kosong / rusak
            when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

            String result = imageService.saveImage(mockMultipartFile, tempDir.toString() + "/");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Successfully saves image and returns path with plain config")
        void whenValidImageInput_thenSavesSuccessfully(@TempDir Path tempDir) throws IOException {
            InputStream inputStream = new ByteArrayInputStream(validImageBytes);
            when(mockMultipartFile.getInputStream()).thenReturn(inputStream);

            String targetFolder = tempDir.toString() + "/";
            String savedPath = imageService.saveImage(mockMultipartFile, targetFolder, false, false);

            assertThat(savedPath).isNotEmpty();
            assertThat(savedPath).startsWith(targetFolder);
            assertThat(savedPath).endsWith(".jpeg");
            
            // Verifikasi fisik file benar-benar terbuat di disk virtual
            assertThat(Files.exists(Path.of(savedPath))).isTrue();
        }

        @Test
        @DisplayName("Successfully applies square canvas padding when fitPicture is true")
        void whenFitPictureIsTrue_thenSavesSuccessfully(@TempDir Path tempDir) throws IOException {
            // Menggunakan gambar dummy persegi panjang (lebar > tinggi)
            BufferedImage rectangleImage = new BufferedImage(10, 5, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(rectangleImage, "jpeg", baos);
            
            when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(baos.toByteArray()));

            String targetFolder = tempDir.toString() + "/";
            String savedPath = imageService.saveImage(mockMultipartFile, targetFolder, true, false);

            assertThat(savedPath).isNotEmpty();
            assertThat(Files.exists(Path.of(savedPath))).isTrue();
        }

        @Test
        @DisplayName("Triggers quality compression loop when isCompressing is true")
        void whenCompressionIsEnabled_thenExecutesWithoutErrors(@TempDir Path tempDir) throws IOException {
            InputStream inputStream = new ByteArrayInputStream(validImageBytes);
            when(mockMultipartFile.getInputStream()).thenReturn(inputStream);

            String targetFolder = tempDir.toString() + "/";
            String savedPath = imageService.saveImage(mockMultipartFile, targetFolder, false, true);

            assertThat(savedPath).isNotEmpty();
            assertThat(Files.exists(Path.of(savedPath))).isTrue();
        }
    }

    // =====================================================================
    // deleteImage()
    // =====================================================================
    @Nested
    @DisplayName("deleteImage()")
    class DeleteImageTests {

        @Test
        @DisplayName("Returns true and deletes file when file exists")
        void whenFileExists_thenDeletesAndReturnsTrue(@TempDir Path tempDir) throws IOException {
            Path dummyFile = tempDir.resolve("target-delete.jpeg");
            Files.write(dummyFile, validImageBytes); // Buat file fisik dummy

            boolean isDeleted = imageService.deleteImage(dummyFile.toString());

            assertThat(isDeleted).isTrue();
            assertThat(Files.exists(dummyFile)).isFalse(); // File harus hilang
        }

        @Test
        @DisplayName("Returns false when target file does not exist")
        void whenFileDoesNotExist_thenReturnsFalse() {
            boolean isDeleted = imageService.deleteImage("ghost-folder/non-existent.jpeg");

            assertThat(isDeleted).isFalse();
        }
    }

    // =====================================================================
    // getImage()
    // =====================================================================
    @Nested
    @DisplayName("getImage()")
    class GetImageTests {

        @Test
        @DisplayName("Returns full server URL with Base64 encoded path if file exists")
        void whenFileExists_thenReturnsServerUrl(@TempDir Path tempDir) throws IOException {
            Path testFile = tempDir.resolve("my-pic.jpeg");
            Files.write(testFile, validImageBytes);

            String result = imageService.getImage(testFile.toString());

            String expectedBase64 = Base64.getEncoder().encodeToString(testFile.toString().getBytes());
            assertThat(result).isEqualTo(HOST_PATH + "/" + expectedBase64);
        }

        @Test
        @DisplayName("Returns empty string if file does not exist or path is empty")
        void whenFileDoesNotExistOrPathEmpty_thenReturnsEmptyString() {
            String resultEmpty = imageService.getImage("");
            String resultGhost = imageService.getImage("missing-folder/pic.jpeg");

            assertThat(resultEmpty).isEmpty();
            assertThat(resultGhost).isEmpty();
        }
    }

    // =====================================================================
    // Timestamp Decoding Tests
    // =====================================================================
    @Nested
    @DisplayName("Timestamp Decoding Tests")
    class TimestampDecodingTests {

        @Test
        @DisplayName("getImageTimeStamp() correctly decodes Base64 filename into plain date string")
        void whenValidUrlPath_thenDecodesTimestamp() {
            String simulatedTimestamp = LocalDateTime.now().toString();
            String base64Filename = Base64.getEncoder().encodeToString(simulatedTimestamp.getBytes());
            
            // Format input: path/to/file/BASE64_STRING.jpeg
            String fullPathInput = "uploads/profile/" + base64Filename + ".jpeg";

            String decodedTimestamp = imageService.getImageTimeStamp(fullPathInput);

            assertThat(decodedTimestamp).isEqualTo(simulatedTimestamp);
        }

        @Test
        @DisplayName("getImageTimeStampByTruePath() decodes accurately when file physically exists")
        void whenTruePathExists_thenDecodesTimestamp(@TempDir Path tempDir) throws IOException {
            String simulatedTimestamp = "2026-06-03T20:00:00";
            String base64Filename = Base64.getEncoder().encodeToString(simulatedTimestamp.getBytes());
            
            Path physicalFile = tempDir.resolve(base64Filename + ".jpeg");
            Files.write(physicalFile, validImageBytes);

            String decodedTimestamp = imageService.getImageTimeStampByTruePath(physicalFile.toString());

            assertThat(decodedTimestamp).isEqualTo(simulatedTimestamp);
        }

        @Test
        @DisplayName("Timestamp methods safely return empty string on empty input")
        void whenInputIsEmpty_thenReturnsEmptyString() {
            assertThat(imageService.getImageTimeStamp("")).isEmpty();
            assertThat(imageService.getImageTimeStamp(null)).isEmpty();
            assertThat(imageService.getImageTimeStampByTruePath("")).isEmpty();
        }
    }
}