package com.moodbites.restfulapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulingService Unit Tests")
class SchedulingServiceTest {

    @Mock
    private OTPService otpService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SchedulingService schedulingService;

    // =====================================================================
    // Logic Execution Tests
    // =====================================================================
    @Nested
    @DisplayName("doSomethingEvery5Secs() Logic")
    class DoSomethingEvery5SecsLogic {

        @Test
        @DisplayName("Executes OTP clearance and Session deletion correctly")
        void whenCalled_thenExecutesCleanupTasks() {
            // Act: Kita panggil methodnya secara langsung
            schedulingService.doSomethingEvery5Secs();

            // Assert: Memastikan kedua service dieksekusi tepat satu kali
            verify(otpService).clearRedundantOTP();
            verify(authService).deleteExpiredSessions();
        }
    }

    // =====================================================================
    // Annotation / Configuration Tests
    // =====================================================================
    @Nested
    @DisplayName("Scheduling Configuration")
    class SchedulingConfiguration {

        @Test
        @DisplayName("Method must be annotated with @Scheduled and fixedRate of 5000ms")
        void whenMethodChecked_thenHasCorrectScheduledAnnotation() throws NoSuchMethodException {
            // Act: Mengambil metadata method menggunakan Java Reflection
            Method method = SchedulingService.class.getMethod("doSomethingEvery5Secs");
            Scheduled scheduledAnnotation = method.getAnnotation(Scheduled.class);

            // Assert: Memastikan anotasi ada dan nilainya tepat
            assertThat(scheduledAnnotation).isNotNull();
            assertThat(scheduledAnnotation.fixedRate()).isEqualTo(5000L);
            
            // Opsional: Memastikan cron tidak digunakan jika memang niatnya pakai fixedRate
            assertThat(scheduledAnnotation.cron()).isEmpty();
        }
    }
}