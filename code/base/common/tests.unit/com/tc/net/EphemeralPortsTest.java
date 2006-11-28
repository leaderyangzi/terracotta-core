/**
 * Copyright (c) 2003-2006 Terracotta, Inc. All rights reserved.
 */
package com.tc.net;

import com.tc.net.EphemeralPorts.Range;

import junit.framework.TestCase;

public class EphemeralPortsTest extends TestCase {

  public void test() {
    Range range = EphemeralPorts.getRange();
    System.out.println(range);
    assertTrue("lower is " + range.getLower(), range.getLower() >= 1024);
    assertTrue("upper is " + range.getUpper(), range.getUpper() <= 65535);
  }

}
