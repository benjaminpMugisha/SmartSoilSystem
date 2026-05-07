package com.rab.smartsoil.alerts;

import com.rab.smartsoil.model.DiseaseAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER
 * ═══════════════════════════════════════════════════════════════
 * Intent: Define a one-to-many dependency so that when the
 *         DiseaseAlertSystem changes state (disease detected),
 *         all registered observers are notified automatically.
 *
 * Subject  : DiseaseAlertSystem
 * Observers: FarmerAlertObserver, AgronomistAlertObserver,
 *            AdminDashboardObserver
 *
 * Benefit: DiseaseAlertSystem is completely decoupled from the
 *          concrete notification channels. New observers (e.g.,
 *          WhatsAppObserver, MinAgriReportObserver) can be added
 *          at runtime without changing any existing code.
 * ═══════════════════════════════════════════════════════════════
 */

// ── Observer Interface ────────────────────────────────────────
public interface AlertObserver {
    /**
     * Called by DiseaseAlertSystem when a disease alert is triggered.
     *
     * @param alert the DiseaseAlert containing plot, disease type, and severity
     */
    void onAlertReceived(DiseaseAlert alert);
}


// ── Subject (Observable) ──────────────────────────────────────
@Component
public class DiseaseAlertSystem {

    private static final Logger log = LoggerFactory.getLogger(DiseaseAlertSystem.class);
    private final List<AlertObserver> observers = new ArrayList<>();

    /**
     * Registers an observer to receive disease notifications.
     *
     * @param observer the observer to add
     */
    public void addObserver(AlertObserver observer) {
        observers.add(observer);
        log.debug("Observer registered: {}", observer.getClass().getSimpleName());
    }

    /**
     * Removes an observer from the notification list.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(AlertObserver observer) {
        observers.remove(observer);
        log.debug("Observer removed: {}", observer.getClass().getSimpleName());
    }

    /**
     * Triggers a disease alert and notifies ALL registered observers.
     * This is the core Subject notification method.
     *
     * @param alert the DiseaseAlert to broadcast
     */
    public void triggerAlert(DiseaseAlert alert) {
        log.warn("[DISEASE ALERT] Plot: {} | Type: {} | Severity: {} | SpreadRisk: {}",
                alert.getPlotId(), alert.getDiseaseType(),
                alert.getSeverity(), alert.getSpreadRisk());

        observers.forEach(observer -> {
            try {
                observer.onAlertReceived(alert);
            } catch (Exception e) {
                log.error("Observer {} failed to process alert: {}",
                        observer.getClass().getSimpleName(), e.getMessage());
            }
        });
    }

    public int getObserverCount() { return observers.size(); }
}


// ── Concrete Observer 1 — Farmer SMS / App Notification ──────
@Component
public class FarmerAlertObserver implements AlertObserver {

    private static final Logger log = LoggerFactory.getLogger(FarmerAlertObserver.class);

    @Override
    public void onAlertReceived(DiseaseAlert alert) {
        String message = buildFarmerMessage(alert);
        // In production: NotificationService.sendSMS(alert.getFarmerPhone(), message)
        log.info("[SMS → FARMER] To: {} | Message: {}", alert.getFarmerPhone(), message);
    }

    private String buildFarmerMessage(DiseaseAlert alert) {
        return String.format(
            "RAB ALERT: %s detected on your plot %s. Severity: %s. " +
            "Prevention: %s. Contact your RAB agronomist immediately.",
            alert.getDiseaseType(), alert.getPlotId(),
            alert.getSeverity(), alert.getPreventionMeasures()
        );
    }
}


// ── Concrete Observer 2 — Agronomist Escalation ───────────────
@Component
public class AgronomistAlertObserver implements AlertObserver {

    private static final Logger log = LoggerFactory.getLogger(AgronomistAlertObserver.class);

    @Override
    public void onAlertReceived(DiseaseAlert alert) {
        if (alert.getSeverity() == DiseaseAlert.Severity.HIGH) {
            log.warn("[ESCALATION → AGRONOMIST] HIGH severity alert on plot {}. " +
                     "District: {}. Immediate field visit required.",
                     alert.getPlotId(), alert.getDistrict());
        } else {
            log.info("[NOTIFY → AGRONOMIST] {} alert on plot {}. Monitor closely.",
                     alert.getSeverity(), alert.getPlotId());
        }
    }
}


// ── Concrete Observer 3 — RAB Admin Dashboard Update ─────────
@Component
public class AdminDashboardObserver implements AlertObserver {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardObserver.class);

    @Override
    public void onAlertReceived(DiseaseAlert alert) {
        log.info("[DASHBOARD → ADMIN] National map updated. Plot: {} | District: {} | " +
                 "Disease: {} | Severity: {} | SpreadRisk: {:.0%}",
                 alert.getPlotId(), alert.getDistrict(),
                 alert.getDiseaseType(), alert.getSeverity(),
                 alert.getSpreadRisk());
    }
}
