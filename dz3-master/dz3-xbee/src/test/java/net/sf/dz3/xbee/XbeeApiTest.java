package net.sf.dz3.xbee;

import junit.framework.TestCase;
import net.sf.dz3.device.sensor.impl.xbee.Parser;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeePacket;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtRequest;
import com.rapplogic.xbee.util.ByteUtils;

public class XbeeApiTest extends TestCase {

    private final Logger logger = Logger.getLogger(getClass());
    
    public void testNothing() {
        
        // To make JUnit happy while other hardware specific tests are disabled
    }
    
    public void testPacketEscape() {
        
        final int[] knownGoodPacket = new int[] {
                0x7E,
                0x00,
                0x0F,
                0x17,
                0x01,
                0x00,
                0x7D, // 0x7D33 is 0x13 escaped
                0x33,
                0xA2,
                0x00,
                0x40,
                0x62,
                0xAC,
                0x98,
                0xFF,
                0xFE,
                0x02,
                0x41,
                0x30,
                0xDC
        };

        NDC.push("testPacketEscape");

        try {

            XBeeAddress64 xbeeAddress = Parser.parse("0013A200.4062AC98");
            ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(xbeeAddress, "A0");
            XBeePacket packet = request.getXBeePacket();

            int[] byteBuffer = packet.getPacket();
            logger.info("Packet: " + ByteUtils.toBase16(byteBuffer));
            
            assertEquals("Byte buffer length mismatch", knownGoodPacket.length, byteBuffer.length);
            
            for (int offset = 0; offset < knownGoodPacket.length; offset++) {
                
                assertEquals("Packet content mismatch @" + offset, knownGoodPacket[offset], byteBuffer[offset]);
            }

        } catch (Throwable t) {

            logger.error("Oops", t);
            fail(t.getMessage());

        } finally {

            NDC.pop();
        }
    }

    public void xtestXbee() throws XBeeException {

        NDC.push("testXBee");

        try {

            XBee xbee = new XBee();

            try {
                xbee.open("/dev/ttyUSB0", 9600);
                
                // Find out who's around
                
                AT(xbee, "MY");
                AT(xbee, "NC");
                AT(xbee, "NI");
                AT(xbee, "NP");
                AT(xbee, "DD");
                AT(xbee, "CH");
                AT(xbee, "ID");
                AT(xbee, "OP");
                AT(xbee, "OI");
                AT(xbee, "NT");
                AT(xbee, "NO");
                AT(xbee, "SD");
                AT(xbee, "NJ");
                AT(xbee, "EE");
                AT(xbee, "AP");
                AT(xbee, "BD");
                AT(xbee, "P0");
                AT(xbee, "VR");
                AT(xbee, "HV");
                AT(xbee, "AI");
                
                AT(xbee, "ND");
                AT(xbee, "AI");
                
                AT(xbee, "AP", 2);
                
                for (int offset = 0; offset < 4; offset++) {

                    String target = "D" + offset;
                    XBeeAddress64 addr64 = new XBeeAddress64(0x00, 0x13, 0xa2, 0x00, 0x40, 0x5d, 0x80, 0x27);
//                  XBeeAddress64 addr64 = new XBeeAddress64("00 13 A2 00 40 5D 80 27");
//                  XBeeAddress16 addr16 = new XBeeAddress16(0x48, 0xFE);
//                  XBeeAddress64 addr64 = new XBeeAddress64(0x00, 0x13, 0xa2, 0x00, 0x40, 0x62, 0xac, 0x98);

                    NDC.push("405D8027:" + offset + " write 5");

                    
                    try {
                        
                        logger.info("creating request to " + addr64);

                        // Send the request to turn on D${offset}
                        ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(addr64, target, new int[] {5});
//                        ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, addr16, true, target, new int[] {5});
                        XBeeResponse rsp = xbee.sendSynchronous(request, 5000);

                        logger.info(target + " response: " + rsp);

                        AtCommandResponse response = (AtCommandResponse) xbee.getResponse();

                        if (response.isOk()) {
                            logger.info("Successfully turned " + target);
                        } else {
                            logger.error("Attempt to turn on " + target + " failed.  Status: " + response.getStatus());
                        }
                        
                    } finally {
                        NDC.pop();
                    }

                    NDC.push("405D8027:" + offset + " read");

                    try {

                        // Query D${offset} status
                        ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(addr64, target);

                        XBeeResponse rsp = xbee.sendSynchronous(request, 10000);

                        logger.info(target + " response: " + rsp);

                        AtCommandResponse response = (AtCommandResponse) xbee.getResponse();

                        if (response.isOk()) {
                            logger.info("Successfully turned " + target);
                        } else {
                            logger.error("Attempt to turn on " + target + " failed.  Status: " + response.getStatus());
                        }
                        
                    } finally {
                        NDC.pop();
                    }

                    NDC.push("405D8027:" + offset + " write 4");

                    try {
                        
                        logger.info("creating request to " + addr64);

                        // Send the request to turn on D${offset}
                        ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(addr64, target, new int[] {4});
//                        ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, addr16, true, target, new int[] {5});
                        XBeeResponse rsp = xbee.sendSynchronous(request, 5000);

                        logger.info(target + " response: " + rsp);

                        AtCommandResponse response = (AtCommandResponse) xbee.getResponse();

                        if (response.isOk()) {
                            logger.info("Successfully turned " + target);
                        } else {
                            logger.error("Attempt to turn on " + target + " failed.  Status: " + response.getStatus());
                        }
                        
                    } finally {
                        NDC.pop();
                    }

                }

            } catch (Throwable t) {
                logger.fatal("Oops", t);
            } finally {

                // shutdown the serial port and associated threads
                xbee.close();
            }

        } finally {
            NDC.pop();
        }
    }
    
    private void AT(XBee xbee, String command) {
        
        NDC.push("AT");
        
        try {
            
            XBeeResponse rsp = xbee.sendSynchronous(new AtCommand(command), 10*1000);

            logger.info(command + " response: " + rsp);
        } catch (Throwable t) {
            logger.fatal(command + " failed", t);
        } finally {
            NDC.pop();
        }
    }

    private void AT(XBee xbee, String command, int value) {
        
        NDC.push("AT");
        
        try {
            
            XBeeResponse rsp = xbee.sendSynchronous(new AtCommand(command, value), 10*1000);

            logger.info(command + " response: " + rsp);
        } catch (Throwable t) {
            logger.fatal(command + " failed", t);
        } finally {
            NDC.pop();
        }
    }
}