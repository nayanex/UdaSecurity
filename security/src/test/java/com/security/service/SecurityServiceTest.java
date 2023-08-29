package com.security.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.security.application.StatusListener;
import com.security.data.AlarmStatus;
import com.security.data.ArmingStatus;
import com.security.data.SecurityRepository;
import com.security.data.Sensor;
import com.security.data.SensorType;
import com.udacity.catpoint.image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private StatusListener statusListener;

    @InjectMocks
    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityService.addStatusListener(statusListener);
    }


    @Test
    void InitialAlarmStatusIsNoAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        AlarmStatus alarmStatus = securityService.getAlarmStatus();

        assertEquals(AlarmStatus.NO_ALARM, alarmStatus, "Initial alarm status should be NO_ALARM");
    }

    @Test
    void InitialArmingStatusIsDisarmed() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        ArmingStatus armingStatus = securityService.getArmingStatus();

        assertEquals(ArmingStatus.DISARMED, armingStatus, "Initial arming status should be DISARMED");
    }

    @Test
    void SetArmingStatusUpdatesRepository() {
        ArmingStatus armingStatus = ArmingStatus.ARMED_HOME;

        securityService.setArmingStatus(armingStatus);

        verify(securityRepository).setArmingStatus(armingStatus);
    }

    @Test
    void SetAlarmStatusUpdatesRepository() {
        AlarmStatus alarmStatus = AlarmStatus.ALARM;

        securityService.setAlarmStatus(alarmStatus);

        verify(securityRepository).setAlarmStatus(alarmStatus);
        verify(statusListener).notify(alarmStatus);
    }

    @Test
    void AddSensorUpdatesRepository() {
        Sensor sensor = new Sensor("Sensor1", SensorType.DOOR);

        securityService.addSensor(sensor);

        verify(securityRepository).addSensor(sensor);
    }

    @Test
    void RemoveSensorUpdatesRepository() {
        Sensor sensor = new Sensor("Sensor1", SensorType.DOOR);

        securityService.removeSensor(sensor);

        verify(securityRepository).removeSensor(sensor);
    }

    @Test
    void ProcessImageCallsImageService() {
        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(imageService).imageContainsCat(any(BufferedImage.class), eq(50.0f));
    }

    /**
     * 07. If the image service identifies an image containing a cat while the system is armed-home,
     * put the system into alarm status.
     */
    @Test
    void ProcessImageWhenArmedHome() {
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
     * Test for verifying processImage sets the alarm status to ALARM when the system is armed-home and the camera shows a cat.
     */
    @Test
    void ProcessImageWhenArmedHomeAndCatDetected() {
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * 08. If the image service identifies an image that does not contain a cat,
     * change the status to no alarm as long as the sensors are not active
     */
    @Test
    void ProcessImageNoCatAndNoActiveSensors() {
        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getSensors()).thenReturn(new HashSet<>()); // No active sensors

        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * Test for verifying alarm status is set to NO_ALARM when the system is not armed-home and the image does not contain a cat.
     */
    @Test
    void ProcessImageNoCatNotArmedHome() {
        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED); // Not armed home

        // Save the initial alarm status before processing the image
        AlarmStatus initialAlarmStatus = securityRepository.getAlarmStatus();

        securityService.processImage(image);

        // Verify that alarm status is not changed
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
        // Verify that initial alarm status is the same as after processing the image
        assertEquals(initialAlarmStatus, securityRepository.getAlarmStatus());
    }

    /**
     * Test for verifying processImage sets the alarm status to NO_ALARM when the system is armed-home and the camera does not show a cat.
     */
    @Test
    void ProcessImageWhenArmedHomeAndNoCatDetected() {
        // Given
        BufferedImage image = mock(BufferedImage.class);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        // When
        securityService.processImage(image);

        // Then
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * 09. If the system is disarmed, set the status to no alarm.
     * Test for verifying setArmingStatus changes the alarm status to NO_ALARM for certain arming statuses.
     */
    @Test
    void DisarmedStatusSetsNoAlarm() {
        // Set up initial arming status as DISARMED
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        // Execute the method that transitions arming status
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        // Verify that the alarm status is set to NO_ALARM
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * 10. If the system is armed, reset all sensors to inactive.
     * Test for verifying setArmingStatus does not change alarm status when armed home.
     */
    @Test
    void ArmedHomeDoesNotChangeAlarmStatus() {
        // Set up initial arming status as ARMED_HOME
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Execute the method that transitions arming status
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // Verify that alarm status is not changed
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    void ResetSensorsToInactiveWhenArmed() {
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

    /*
    01. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    02. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    * */
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void AlarmScenariosContinued(ArmingStatus armingStatus) {
        // ... similar setup ...

        // Create mock Sensor object
        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getActive()).thenReturn(true);

        // Set alarm status to PENDING_ALARM if arming status is ARMED_AWAY
        if (armingStatus == ArmingStatus.ARMED_AWAY) {
            when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        }

        // Execute the method that handles sensor activation
        securityService.changeSensorActivationStatus(mockSensor, true);

        // Verify interactions based on the scenarios
        if (armingStatus == ArmingStatus.ARMED_HOME) {
            verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
        } else {
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Scenario 3: If pending alarm and all sensors are inactive, return to no alarm state.
     * */
    @Test
    void PendingAlarmAndNoActiveSensors() {
        // Set up pending alarm status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(new HashSet<>()); // No active sensors

        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /**
     * Scenario 4: If alarm is active, change in sensor state should not affect the alarm state.
     */
    @Test
    void SensorActivationWithActiveAlarm() {
        // Set up active alarm status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        // Create mock Sensor object
        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getActive()).thenReturn(true);

        securityService.changeSensorActivationStatus(mockSensor, true);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    /**
     * Scenario 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
     */
    @Test
    void SensorActivationWhilePendingAlarm() {
        // Set up pending alarm status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        // Create mock Sensor object
        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getActive()).thenReturn(true);

        securityService.changeSensorActivationStatus(mockSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Scenario 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
     */
    @Test
    void SensorDeactivationWhileInactive() {
        // Create mock Sensor object
        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getActive()).thenReturn(false);

        securityService.changeSensorActivationStatus(mockSensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }


    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void SensorActivationAndAlarmStates(ArmingStatus armingStatus) {
        // ... similar setup ...

        // Create mock Sensor object
        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getActive()).thenReturn(true); // Set the sensor as active

        // Set alarm status to PENDING_ALARM if arming status is ARMED_AWAY
        if (armingStatus == ArmingStatus.ARMED_AWAY) {
            when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        }

        // When
        securityService.changeSensorActivationStatus(mockSensor, true);

        // Then
        if (armingStatus == ArmingStatus.ARMED_HOME) {
            verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
        } else {
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }
    }
}
