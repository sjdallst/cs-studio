/**
 *
 */
package org.csstudio.platform.libs.dal.tests.valuedelivery;

import org.epics.css.dal.context.ConnectionState;

/**
 * Test to demonstrate the callback sequence of the simple dal broker api.
 * Exact sequence and count of callbacks is not determined, therefore we check only the occurrence of certain connection states.
 * 
 * Uses
 * record(calc, "SawCalc0") {
   field(DESC, "$")
   field(SCAN, "1 second")
   field(PHAS, "1")
   field(CALC, "A<D?A+B:C")
   field(INPA, "SawCalc0.VAL NPP MS")
   field(INPB, "1")
   field(INPC, "10")
   field(INPD, "75")
   field(EGU, "Counts")
   field(HOPR, "100")
   field(LOPR, "0")
   field(HIHI, "70")
   field(LOLO, "15")
   field(HIGH, "65")
   field(LOW, "20")
   field(HHSV, "MAJOR")
   field(LLSV, "MAJOR")
   field(HSV, "MINOR")
   field(LSV, "MINOR")
}

record(calc, "ConstantPV") {
   field(DESC, "$")
   field(SCAN, "10 second")
   field(PHAS, "10")
   field(CALC, "A")
   field(INPA, "66")
   field(EGU, "Counts")
   field(HOPR, "100")
   field(LOPR, "0")
   field(HIHI, "70")
   field(LOLO, "15")
   field(HIGH, "65")
   field(LOW, "20")
   field(HHSV, "MAJOR")
   field(LLSV, "MAJOR")
   field(HSV, "MINOR")
   field(LSV, "MINOR")
}

record(ai, "ConstantPVPassive") {
   field(DESC, "$")
   field(SCAN, "Passive")
   field(PINI, "NO")
   field(EGU, "Counts")
   field(VAL, "66")
   field(HOPR, "100")
   field(LOPR, "0")
   field(HIHI, "70")
   field(LOLO, "15")
   field(HIGH, "65")
   field(LOW, "20")
   field(HHSV, "MAJOR")
   field(LLSV, "MAJOR")
   field(HSV, "MINOR")
   field(LSV, "MINOR")
}

 */
public class ConnectionSequenceTest extends AbstractDalBoundaryTest {
    
    private static final String SOME_PV = "CMTBSTC1K10_temp";

    // disabled outside desy
    public void notestConnectSomePV() throws Exception {
        registerChannelListenerForPV(SOME_PV);
        Thread.sleep(SLEEP_TIME_MSEC);
        deregisterListenerForPV(SOME_PV);
        
        assertTrue(hasConnectionState(ConnectionState.CONNECTING));
        assertTrue(hasConnectionState(ConnectionState.CONNECTED));
        assertTrue(hasConnectionState(ConnectionState.OPERATIONAL));
        
        // this pv has a moving value, therefore we cannot check value or alarm state and the like.
    }
    
    public void testConnectCalculatedPV() throws Exception {
        registerChannelListenerForPV(SAW_CALC_0);
        Thread.sleep(SLEEP_TIME_MSEC);
        deregisterListenerForPV(SAW_CALC_0);
        
        assertTrue(hasConnectionState(ConnectionState.CONNECTING));
        assertTrue(hasConnectionState(ConnectionState.CONNECTED));
        assertTrue(hasConnectionState(ConnectionState.OPERATIONAL));
        
        // this pv has a moving value, therefore we cannot check value or alarm state and the like.
    }
    
    public void testConnectConstantPV() throws Exception {
        registerChannelListenerForPV(CONSTANT_PV);
        Thread.sleep(SLEEP_TIME_MSEC);
        deregisterListenerForPV(CONSTANT_PV);
        
        assertTrue(hasConnectionState(ConnectionState.CONNECTING));
        assertTrue(hasConnectionState(ConnectionState.CONNECTED));
        assertTrue(hasConnectionState(ConnectionState.OPERATIONAL));
        Result result = getResultFor(ConnectionState.OPERATIONAL);
        assertTrue(result.condition.isWarning());
        assertEquals("66.0", result.anyValue.toString());
    }
    
    public void testConnectPassivePV() throws Exception {
        registerChannelListenerForPV(CONSTANT_PV_PASSIVE);
        Thread.sleep(SLEEP_TIME_MSEC);
        deregisterListenerForPV(CONSTANT_PV_PASSIVE);
        
        assertTrue(hasConnectionState(ConnectionState.CONNECTING));
        assertTrue(hasConnectionState(ConnectionState.CONNECTED));
        assertTrue(hasConnectionState(ConnectionState.OPERATIONAL));
        Result result = getResultFor(ConnectionState.OPERATIONAL);
        assertTrue(result.condition.isError());
    }
    
    public void testConnectNotExistingPV() throws Exception {
        registerChannelListenerForPV(DOES_NOT_EXIST_NAME);
        Thread.sleep(SLEEP_TIME_MSEC);
        deregisterListenerForPV(DOES_NOT_EXIST_NAME);
        
        assertTrue(hasConnectionState(ConnectionState.CONNECTING));
        assertTrue(hasConnectionState(ConnectionState.CONNECTION_FAILED));
    }
    
}