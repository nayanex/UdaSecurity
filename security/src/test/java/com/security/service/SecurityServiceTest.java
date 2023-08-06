package com.security.service;

import com.security.data.AlarmStatus;
import com.security.data.ArmingStatus;
import com.security.data.SecurityRepository;
import com.security.data.Sensor;
import com.security.data.SensorType;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private FakeImageService imageService;

    @InjectMocks
    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper method to create sample sensors for testing
    private Set<Sensor> createSampleSensors() {
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(new Sensor("Sensor1", SensorType.DOOR));
        sensors.add(new Sensor("Sensor2", SensorType.WINDOW));
        return sensors;
    }

    // --------------------- Tests for SecurityService methods ---------------------

    /**
     * Test for verifying initial alarm status is NO_ALARM.
     */
    @Test
    void testInitialAlarmStatusIsNoAlarm() {
        // Given
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        // When
        AlarmStatus alarmStatus = securityService.getAlarmStatus();

        // Then
        assertEquals(AlarmStatus.NO_ALARM, alarmStatus, "Initial alarm status should be NO_ALARM");
    }

    /**
     * Test for verifying initial arming status is DISARMED.
     */
    @Test
    void testInitialArmingStatusIsDisarmed() {
        // Given
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        // When
        ArmingStatus armingStatus = securityService.getArmingStatus();

        // Then
        assertEquals(ArmingStatus.DISARMED, armingStatus, "Initial arming status should be DISARMED");
    }

    /**
     * Test for verifying setArmingStatus updates the repository.
     */
    @Test
    void testSetArmingStatusUpdatesRepository() {
        // Given
        ArmingStatus armingStatus = ArmingStatus.ARMED_HOME;

        // When
        securityService.setArmingStatus(armingStatus);

        // Then
        verify(securityRepository).setArmingStatus(armingStatus);
    }

    /**
     * Test for verifying setAlarmStatus updates the repository.
     */
    @Test
    void testSetAlarmStatusUpdatesRepository() {
        // Given
        AlarmStatus alarmStatus = AlarmStatus.ALARM;

        // When
        securityService.setAlarmStatus(alarmStatus);

        // Then
        verify(securityRepository).setAlarmStatus(alarmStatus);
    }

    /**
     * Test for verifying addSensor updates the repository.
     */
    @Test
    void testAddSensorUpdatesRepository() {
        // Given
        Sensor sensor = new Sensor("Sensor1", SensorType.DOOR);

        // When
        securityService.addSensor(sensor);

        // Then
        verify(securityRepository).addSensor(sensor);
    }

    /**
     * Test for verifying removeSensor updates the repository.
     */
    @Test
    void testRemoveSensorUpdatesRepository() {
        // Given
        Sensor sensor = new Sensor("Sensor1", SensorType.DOOR);

        // When
        securityService.removeSensor(sensor);

        // Then
        verify(securityRepository).removeSensor(sensor);
    }


    /**
     * Test for verifying processImage calls imageService with the correct parameters.
     */
    @Test
    void testProcessImageCallsImageService() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        // When
        securityService.processImage(image);

        // Then
        verify(imageService).imageContainsCat(any(BufferedImage.class), eq(50.0f));
    }

    /**
     * 7. If the image service identifies an image containing a cat while the system is armed-home,
     * put the system into alarm status.
     */
    @Test
    void testProcessImageWhenArmedHome() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * 8. If the image service identifies an image that does not contain a cat,
     * change the status to no alarm as long as the sensors are not active
     */
    @Test
    void testProcessImageNoCatAndInactiveSensors() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityRepository.getSensors()).thenReturn(createSampleSensors());

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
     * Test for verifying processImage sets the alarm status to ALARM when the system is armed-home and the camera shows a cat.
     */
    @Test
    void testProcessImageWhenArmedHomeAndCatDetected() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Test for verifying alarm status is set to NO_ALARM when the system is not armed-home and the image does not contain a cat.
     */
    @Test
    void testProcessImageWhenNotArmedHomeAndNoCatDetected() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * Test for verifying processImage sets the alarm status to NO_ALARM when the system is armed-home and the camera does not show a cat.
     */
    @Test
    void testProcessImageWhenArmedHomeAndNoCatDetected() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


    // --------------------- Parameterized Tests for setArmingStatus method ---------------------

    /**
     * 9. If the system is disarmed, set the status to no alarm.
     * Test for verifying setArmingStatus changes the alarm status to NO_ALARM for certain arming statuses.
     */
    @ParameterizedTest
    @MethodSource("armingStatusParams")
    void testSetArmingStatusChangesAlarmStatusToNoAlarm(ArmingStatus armingStatus) {
        // Given
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);

        // When
        securityService.setArmingStatus(armingStatus);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    private static Stream<ArmingStatus> armingStatusParams() {
        return Stream.of(ArmingStatus.DISARMED, ArmingStatus.ARMED_AWAY);
    }

    /**
     * 10. If the system is armed, reset all sensors to inactive.
     * Test for verifying setArmingStatus does not change alarm status when armed home.
     */
    @Test
    void testSetArmingStatusDoesNotChangeAlarmStatusWhenArmedHome() {
        // Given
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // When
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // Then
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    // 10. Test for verifying all sensors are reset to inactive when the system is armed
    @Test
    void testResetSensorsToInactiveWhenArmed() {
        // Given
        Sensor sensor1 = new Sensor("Sensor1", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Sensor2", SensorType.WINDOW);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Set both sensors to active
        sensor1.setActive(true);
        sensor2.setActive(true);

        // When
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // Then
        assertFalse(sensor1.getActive(), "Sensor1 should be reset to inactive");
        assertFalse(sensor2.getActive(), "Sensor2 should be reset to inactive");
    }

    // Test for verifying sensor state changes do not affect the alarm status when the alarm is active
    @Test
    void testSensorStateChangesDoNotAffectAlarmStatusWhenAlarmActive() {
        // Given
        Sensor sensor1 = new Sensor("Sensor1", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Sensor2", SensorType.WINDOW);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Set both sensors to active
        sensor1.setActive(true);
        sensor2.setActive(true);

        // Activate the first sensor, alarm should go to PENDING_ALARM
        securityService.changeSensorActivationStatus(sensor1, true);

        // Activate the second sensor, alarm should go to ALARM
        securityService.changeSensorActivationStatus(sensor2, true);

        // Set both sensors to inactive, it should not affect the alarm status
        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, false);

        // When
        AlarmStatus alarmStatusAfterSensorChanges = securityService.getAlarmStatus();

        // Then
        assertEquals(AlarmStatus.ALARM, alarmStatusAfterSensorChanges, "Alarm status should remain ALARM after sensor changes");
    }


    // --------------------- Parameterized Tests for sensor activation scenarios ---------------------

    /**
     * Test for various scenarios of sensor activation and alarm status changes accordingly.
     */
    @ParameterizedTest
    @MethodSource("alarmStatusAndSensorActivationParams")
    void testSensorActivation(ArmingStatus armingStatus, AlarmStatus alarmStatus, boolean initialSensorState, boolean activated, AlarmStatus expectedAlarmStatus) {
        // Given
        Sensor sensor = new Sensor("Sensor1", SensorType.DOOR);
        Set<Sensor> sensors = createSampleSensors();
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        securityService.changeSensorActivationStatus(sensor, initialSensorState);

        // When
        securityService.changeSensorActivationStatus(sensor, activated);

        // Then
        verify(securityRepository).setAlarmStatus(expectedAlarmStatus);
    }

    private static Stream<Arguments> alarmStatusAndSensorActivationParams() {
        return Stream.of(
                // 1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.NO_ALARM, false, true, AlarmStatus.PENDING_ALARM),
                // 2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM, false, true, AlarmStatus.ALARM),
                // 3. If pending alarm and all sensors are inactive, return to no alarm state.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM, true, false, AlarmStatus.NO_ALARM),
                // 4. If alarm is active, change in sensor state should not affect the alarm state.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.ALARM, true, false, AlarmStatus.ALARM),
                // 5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM, true, true, AlarmStatus.ALARM),
                // 6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM, false, false, AlarmStatus.PENDING_ALARM),
                Arguments.of(ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM, true, false, AlarmStatus.PENDING_ALARM),
                Arguments.of(ArmingStatus.ARMED_AWAY, AlarmStatus.PENDING_ALARM, true, true, AlarmStatus.PENDING_ALARM),
                Arguments.of(ArmingStatus.DISARMED, AlarmStatus.NO_ALARM, true, true, AlarmStatus.PENDING_ALARM),
                Arguments.of(ArmingStatus.DISARMED, AlarmStatus.NO_ALARM, true, false, AlarmStatus.NO_ALARM)
        ).map(Arguments::of);
    }
}
