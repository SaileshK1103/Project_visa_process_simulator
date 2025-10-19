package MVC.simu.model;

import MVC.controller.IControllerMtoV;
import MVC.simu.framework.EventList;
import MVC.simu.framework.Trace;
import MVC.view.IVisualisation;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Negexp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicePointTest {
    private ServicePoint servicePoint;
    private EventList eventList;
    private EventType eventType;
    private IControllerMtoV controller;

    @BeforeEach
    void setUp() {

        // Initialize Trace.traceLevel to avoid NullPointerException
        Trace.setTraceLevel(Trace.Level.INFO);

        // Use a real generator
        ContinuousGenerator generator = new Negexp(5.0);

        // Use fakes/stubs for other dependencies
        eventList = new EventList();
        eventType = EventType.ARRIVAL; // Use a real enum value

        controller = new IControllerMtoV() {
            @Override
            public void showEndTime(double time) {}

            @Override
            public void visualiseCustomer() {}

            @Override
            public void updateQueueStatus(int servicePointId, int queueSize) {}

            @Override
            public void displayResults(String resultsText) {}

            @Override
            public void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime) {}

            @Override
            public IVisualisation getVisualisation() {return null;}
        };

        servicePoint = new ServicePoint(generator, eventList, eventType, controller);
    }

    @Test
    void testAddQueue_IncreasesQueueSize() {
        int numEmployees = servicePoint.getNumEmployees();
        for (int i = 0; i < numEmployees + 1; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        assertTrue(servicePoint.getQueueSize() > 0);
    }

    @Test
    void testRemoveQueue_DecreasesQueueSize() {
        ApplicationAsCustomer app = new ApplicationAsCustomer(true, false);
        servicePoint.addQueue(app);
        servicePoint.removeQueue();
        assertEquals(0, servicePoint.getQueueSize());
    }

    @Test
    void testIsReserved_ReturnsTrueWhenFull() {
        for (int i = 0; i < servicePoint.getNumEmployees(); i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(false, true));
        }
        assertTrue(servicePoint.isReserved());
    }

    @Test
    void testIsOnQueue_ReturnsTrueWhenNotEmpty() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add more customers than employees
        for (int i = 0; i < numEmployees + 1; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(false, true));
        }
        assertTrue(servicePoint.isOnQueue());
    }

    @Test
    void testGetTotalDepartures_IncreasesOnRemove() {
        ApplicationAsCustomer app = new ApplicationAsCustomer(true, true);
        servicePoint.addQueue(app);
        servicePoint.removeQueue();
        assertEquals(1, servicePoint.getTotalDepartures());
    }

    @Test
    void testGetAverageWaitingTime_ZeroIfNoDepartures() {
        assertEquals(0.0, servicePoint.getAverageWaitingTime());
    }

    @Test
    void testGetMaxQueueLength_TracksMax() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add more applications than employees
        for (int i = 0; i < numEmployees + 2; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        // The queue should have reached size 2 at its maximum
        assertEquals(2, servicePoint.getMaxQueueLength());
    }

    @Test
    void testGetUtilization_ReturnsZeroIfNoBusyTime() {
        assertEquals(0.0, servicePoint.getUtilization(100.0));
    }

    @Test
    void testGetNumEmployees_ReturnsDefault() {
        assertEquals(5, servicePoint.getNumEmployees());
    }

    @Test
    void testAdjustEmployees_ChangesEmployeeCount() {
        servicePoint.adjustEmployees(10);
        assertEquals(10, servicePoint.getNumEmployees());
    }

    @Test
    void testCheckBottleneck_IncreasesEmployeesIfRequired() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add enough applications so the queue size exceeds 15
        for (int i = 0; i < numEmployees + 16; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        // The number of employees should have increased by 1
        assertEquals(numEmployees + 1, servicePoint.getNumEmployees());
    }

    @Test
    void testSetAndGetEventTypeScheduled() {
        servicePoint.setEventTypeScheduled(EventType.ARRIVAL);
        assertEquals(EventType.ARRIVAL, servicePoint.getEventTypeScheduled());
    }

    @Test
    void testGetServicePointName_ReturnsSPNames() {
        assertNotNull(servicePoint.getServicePointName());
    }
}