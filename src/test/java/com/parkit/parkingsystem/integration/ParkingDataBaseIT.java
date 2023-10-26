package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingService parkingService;
    private static final String regNumber = "ABCDEF";
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        dataBaseTestConfig = new DataBaseTestConfig();
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        dataBasePrepareService.clearDataBaseEntries();

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @AfterAll
    public static void tearDown(){

    }

    /**
     * Checks that the ticket has been saved in DB and parking spot availability has been updated
     * @throws Exception May throw exception when reading vehicle reg number
     */
    @Test
    public void testParkingACar() throws Exception{
        // GIVEN
        // Done in setUpPerTest()

        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket(regNumber);
        assertNotNull(ticket);
        assertFalse(ticket.getParkingSpot().isAvailable());
    }

    /**
     * Checks that the ticket has an out time (since there already are several tests to check the fare calculation
     * then this test doesn't check the fare price)
     * @throws Exception May throw exception when reading vehicle reg number
     */
    @Test
    public void testParkingLotExit() throws Exception{
        // GIVEN
        testParkingACar();
        Thread.sleep(1000);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket(regNumber);
        assertTrue(ticket.getOutTime().getTime() > 0);
        // assertEquals(0.0, ticket.getPrice()); //parking time is less than 30 minutes so it's free
    }

    /**
     * Checks that a recurring user has several tickets saved in DB, it means a discount is applied when calculating
     * the fare price (since there already is a test to check the fare calculation with a discount, the calculation
     * is not tested here)
     * @throws Exception May throw exception when reading vehicle reg number
     */
    @Test
    public void testParkingLotExitRecurringUser() throws Exception{
        // GIVEN
        testParkingLotExit();
        parkingService.processIncomingVehicle();
        Thread.sleep(1000);

        // WHEN
        parkingService.processExitingVehicle();
        int numberOfTicket = ticketDAO.getNbTickets(regNumber);

        // THEN
        assertEquals(2, numberOfTicket);
    }

}
