package com.moodbites.restfulapi.service;

import com.moodbites.restfulapi.model.OTP;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.repository.OTPRepository;
import com.moodbites.restfulapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTPService Unit Tests")
class OTPServiceTest {

    @Mock
    private OTPRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OTPService otpService;

    private User mockUser;
    private OTP mockOtp;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-uuid-123");
        mockUser.setVerifiedAt(null); // Default belum diverifikasi

        mockOtp = new OTP();
        mockOtp.setId("otp-uuid-123");
        mockOtp.setCode("1234");
        mockOtp.setUserId(mockUser);
        mockOtp.setValidUntil(LocalDateTime.now().plusMinutes(5));

        // Relasi dua arah
        mockUser.setOtp(mockOtp);
    }

    // =====================================================================
    // deleteOTP()
    // =====================================================================
    @Nested
    @DisplayName("deleteOTP()")
    class DeleteOTP {

        @Test
        @DisplayName("Deletes OTP and deletes User when verifiedAt is null and bypass is false")
        void whenUserNotVerifiedAndBypassFalse_thenDeletesBoth() {
            otpService.deleteOTP(mockOtp, false);

            // Validasi relasi diputus
            assertThat(mockUser.getOtp()).isNull();
            assertThat(mockOtp.getUserId()).isNull();

            // Validasi repository dipanggil
            verify(otpRepository).delete(mockOtp);
            verify(userRepository).delete(mockUser);
        }

        @Test
        @DisplayName("Deletes OTP but keeps User when user is already verified")
        void whenUserVerified_thenDeletesOtpOnly() {
            mockUser.setVerifiedAt(LocalDateTime.now());

            otpService.deleteOTP(mockOtp, false);

            verify(otpRepository).delete(mockOtp);
            verify(userRepository, never()).delete(any(User.class)); // User aman
        }

        @Test
        @DisplayName("Deletes OTP but keeps User when bypassDeleteUser is true")
        void whenBypassTrue_thenDeletesOtpOnly() {
            otpService.deleteOTP(mockOtp, true);

            verify(otpRepository).delete(mockOtp);
            verify(userRepository, never()).delete(any(User.class)); // User aman karena di-bypass
        }
    }

    // =====================================================================
    // generateOTP()
    // =====================================================================
    @Nested
    @DisplayName("generateOTP()")
    class GenerateOTP {

        @Test
        @DisplayName("Clears existing OTP, generates 4-digit code, and saves new OTP")
        void whenCalled_thenGeneratesAndSavesOTP() {
            // Mock clearExistingOTP agar tidak menghapus user sungguhan di test ini
            when(otpRepository.findByUserId(mockUser)).thenReturn(Optional.empty());

            OTP newOtp = otpService.generateOTP(mockUser);

            // Validasi format OTP (4 digit)
            assertThat(newOtp.getCode()).hasSize(4);
            assertThat(newOtp.getCode()).matches("\\d{4}"); 

            // Validasi expiry time ter-set
            assertThat(newOtp.getValidUntil()).isAfter(LocalDateTime.now());
            assertThat(newOtp.getUserId()).isEqualTo(mockUser);

            verify(otpRepository).save(newOtp);
        }
    }

    // =====================================================================
    // refreshOTP()
    // =====================================================================
    @Nested
    @DisplayName("refreshOTP()")
    class RefreshOTP {

        @Test
        @DisplayName("Updates existing OTP code and expiry when OTP exists")
        void whenOtpExists_thenUpdatesAndSaves() {
            when(otpRepository.findByUserId(mockUser)).thenReturn(Optional.of(mockOtp));
            String oldCode = mockOtp.getCode();

            Optional<OTP> result = otpService.refreshOTP(mockUser);

            assertThat(result).isPresent();
            
            // Format kode baru harus 4 digit
            assertThat(result.get().getCode()).hasSize(4);
            // Tanggal kadaluarsa harus diperbarui (kita asumsikan isAfter berlaku)
            assertThat(result.get().getValidUntil()).isAfter(LocalDateTime.now());
            
            verify(otpRepository).save(mockOtp);
        }

        @Test
        @DisplayName("Returns empty when OTP does not exist")
        void whenOtpDoesNotExist_thenReturnsEmpty() {
            when(otpRepository.findByUserId(mockUser)).thenReturn(Optional.empty());

            Optional<OTP> result = otpService.refreshOTP(mockUser);

            assertThat(result).isEmpty();
            verify(otpRepository, never()).save(any(OTP.class));
        }
    }

    // =====================================================================
    // clearExistingOTP()
    // =====================================================================
    @Nested
    @DisplayName("clearExistingOTP()")
    class ClearExistingOTP {

        @Test
        @DisplayName("Calls deleteOTP when an existing OTP is found")
        void whenOtpExists_thenDeletesIt() {
            when(otpRepository.findByUserId(mockUser)).thenReturn(Optional.of(mockOtp));

            otpService.clearExistingOTP(mockUser);

            // Memastikan deleteOTP dijalankan (bypassDeleteUser = false)
            verify(otpRepository).delete(mockOtp);
            verify(userRepository).delete(mockUser); // Karena mockUser belum verified
        }
    }

    // =====================================================================
    // clearRedundantOTP()
    // =====================================================================
    @Nested
    @DisplayName("clearRedundantOTP()")
    class ClearRedundantOTP {

        @Test
        @DisplayName("Deletes OTPs that are past the 30-minute clearance window")
        void whenOtpsAreRedundant_thenDeletesThem() {
            // Skenario: validUntil sudah berlalu lebih dari 30 menit
            OTP redundantOtp = new OTP();
            redundantOtp.setId("old-otp");
            redundantOtp.setUserId(mockUser);
            // Set kedaluwarsa 40 menit yang lalu (sudah lewat dari OTP_CLEAR 30 menit)
            redundantOtp.setValidUntil(LocalDateTime.now().minusMinutes(40));

            OTP activeOtp = new OTP();
            activeOtp.setId("new-otp");
            activeOtp.setUserId(new User());
            // Set kedaluwarsa baru saja (belum lewat 30 menit)
            activeOtp.setValidUntil(LocalDateTime.now().minusMinutes(10));

            when(otpRepository.findAll()).thenReturn(List.of(redundantOtp, activeOtp));

            otpService.clearRedundantOTP();

            // redundantOtp harus dihapus
            verify(otpRepository).delete(redundantOtp);
            // activeOtp tidak boleh dihapus
            verify(otpRepository, never()).delete(activeOtp);
        }
    }

    // =====================================================================
    // verifyOTP()
    // =====================================================================
    @Nested
    @DisplayName("verifyOTP()")
    class VerifyOTP {

        @Test
        @DisplayName("Returns User and deletes OTP when userId, code, and expiry match")
        void whenValidCredentials_thenReturnsUserAndDeletesOtp() {
            when(otpRepository.findAll()).thenReturn(List.of(mockOtp));

            Optional<User> result = otpService.verifyOTP("user-uuid-123", "1234");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("user-uuid-123");
            
            // Validasi OTP dihapus (bypassDeleteUser = true, jadi user tidak ikut dihapus)
            verify(otpRepository).delete(mockOtp);
            verify(userRepository, never()).delete(mockUser);
        }

        @Test
        @DisplayName("Returns empty when OTP has expired")
        void whenOtpExpired_thenReturnsEmpty() {
            mockOtp.setValidUntil(LocalDateTime.now().minusMinutes(1)); // Sudah expired
            when(otpRepository.findAll()).thenReturn(List.of(mockOtp));

            Optional<User> result = otpService.verifyOTP("user-uuid-123", "1234");

            assertThat(result).isEmpty();
            verify(otpRepository, never()).delete(mockOtp);
        }

        @Test
        @DisplayName("Returns empty when code is wrong")
        void whenCodeWrong_thenReturnsEmpty() {
            when(otpRepository.findAll()).thenReturn(List.of(mockOtp));

            Optional<User> result = otpService.verifyOTP("user-uuid-123", "9999");

            assertThat(result).isEmpty();
            verify(otpRepository, never()).delete(mockOtp);
        }

        @Test
        @DisplayName("Returns empty when userId does not match")
        void whenUserIdWrong_thenReturnsEmpty() {
            when(otpRepository.findAll()).thenReturn(List.of(mockOtp));

            Optional<User> result = otpService.verifyOTP("wrong-user-uuid", "1234");

            assertThat(result).isEmpty();
            verify(otpRepository, never()).delete(mockOtp);
        }
    }
}