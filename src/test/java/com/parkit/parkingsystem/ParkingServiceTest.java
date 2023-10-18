package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }


    /**
     * Checks that the methods updateParking and saveTicket are called once when processing an incoming vehicle
     * @throws Exception May throw exception when reading vehicle type
     */
    @Test
    public void testProcessIncomingVehicle() throws Exception{
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    /**
     * Checks that a parking spot is found when looking for one, returned object ParkingSpot is not null
     * and its id=1 and it is available
     * @throws Exception May throw exception when reading vehicle type
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // WHEN
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNotNull(parkingSpot);
        assertEquals(parkingSpot.getId(), 1);
        assertTrue(parkingSpot.isAvailable());
    }

    /**
     * Checks that no parking spot is found when looking for a parking spot, returned object ParkingSpot is null
     * @throws Exception May throw exception when reading type of vehicle
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        // WHEN
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNull(parkingSpot);
    }

    /**
     * Checks that no parking spot is found when typing a wrong type of vehicle, returned object ParkingSpot is null
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(3); //.thenThrow(IllegalArgumentException.class);

        // WHEN
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNull(parkingSpot);
    }

    /**
     * Checks that the method updateParking is called once when processing an exiting vehicle
     * @throws Exception May throw exception when reading vehicle reg number
     */
    @Test
    public void processExitingVehicleTest() throws Exception{
        // GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(ticket.getVehicleRegNumber());
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTickets(ticket.getVehicleRegNumber())).thenReturn(1);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    /**
     * Checks that the method updateParking is not called when processing an exiting vehicle
     * @throws Exception May throw exception when readVehicleRegistrationNumber
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{
        // GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(ticket.getVehicleRegNumber());
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getNbTickets(ticket.getVehicleRegNumber())).thenReturn(1);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }
}
