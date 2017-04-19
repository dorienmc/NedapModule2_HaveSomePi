package com.nedap.university.clientAndServer.commands.helpers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 13/04/2017.
 */
public class MDNSdataTest {
  MDNSdata mdnSdata;
  MDNSdata mdnSdataFromArr;
  int portIn = 9292;
  int portOut = 9293;
  String hostName = "8";
  byte[] data = {0x00,0x00,0x24,0x4C,0x00,0x00,0x24,0x4D,0x38};

  @Before
  public void setUp() throws Exception {
    mdnSdata = new MDNSdata(portIn, portOut, hostName);
    mdnSdataFromArr = new MDNSdata(data);
  }

  @Test
  public void getInPort() throws Exception {
    assertEquals(portIn, mdnSdata.getInPort());
    assertEquals(portIn, mdnSdataFromArr.getInPort());
  }

  @Test
  public void getOutPort() throws Exception {
    assertEquals(portOut, mdnSdata.getOutPort());
    assertEquals(portOut, mdnSdataFromArr.getOutPort());
  }

  @Test
  public void getHostname() throws Exception {
    assertEquals(hostName, mdnSdata.getHostname());
    assertEquals(hostName, mdnSdataFromArr.getHostname());
  }

  @Test
  public void getData() throws Exception {
    assertArrayEquals(data, mdnSdata.getData());
    assertArrayEquals(data,mdnSdataFromArr.getData());
  }

}