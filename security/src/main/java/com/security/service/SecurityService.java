package com.security.service;

import com.security.application.StatusListener;
import com.security.data.AlarmStatus;
import com.security.data.ArmingStatus;
import com.security.data.SecurityRepository;
import com.security.data.Sensor;
import com.udacity.catpoint.image.service.ImageService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class SecurityService {
    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
            resetSensorsInactive();
        } else if (armingStatus == ArmingStatus.ARMED_HOME || armingStatus == ArmingStatus.ARMED_AWAY) {
            resetSensorsInactive();
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    private void catDetected(Boolean cat) {
        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat && anySensorsActive()) {
            setAlarmStatus(AlarmStatus.NO_ALARM); // If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active
        }
    }

    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void setAlarmStatus(AlarmStatus status) {
        if (status == AlarmStatus.PENDING_ALARM && anySensorsActive()) {
            status = AlarmStatus.NO_ALARM;
        }
        final AlarmStatus finalStatus = status;
        securityRepository.setAlarmStatus(finalStatus);
        statusListeners.forEach(sl -> sl.notify(finalStatus));
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        boolean wasActive = sensor.getActive();

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (active) {
            handleSensorActivated(wasActive);
        } else {
            handleSensorDeactivated(wasActive);
        }
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    public void processImage(BufferedImage currentCameraImage) {
        boolean catDetected = imageService.imageContainsCat(currentCameraImage, 50.0f);
        if (getArmingStatus() == ArmingStatus.ARMED_HOME) {
            catDetected(catDetected);
        }
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

    private boolean anySensorsActive() {
        return securityRepository.getSensors().stream().noneMatch(Sensor::getActive);
    }

    private void resetSensorsInactive() {
        for (Sensor sensor : securityRepository.getSensors()) {
            sensor.setActive(false);
        }
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    private void handleSensorActivated(boolean wasActive) {
        if (getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }

        if (wasActive && getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (getAlarmStatus() == AlarmStatus.NO_ALARM) {
            setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }
    }

    private void handleSensorDeactivated(boolean wasActive) {
        if (getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }

        if (getAlarmStatus() == AlarmStatus.ALARM) {
            return; // If alarm is active, change in sensor state should not affect the alarm state
        }

        if (wasActive && getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else if (getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }
}
