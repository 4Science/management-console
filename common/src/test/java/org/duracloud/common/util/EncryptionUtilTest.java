package org.duracloud.common.util;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Tests encryption utilities.
 *
 * @author Bill Branan
 */
public class EncryptionUtilTest {

    EncryptionUtil encryptionUtil;

    @Before
    public void setUp() throws Exception {
        encryptionUtil = new EncryptionUtil();
    }

    @Test
    public void testEncryption() throws Exception {
        String text = "Test Content";
        String encryptedText = encryptionUtil.encrypt(text);
        assertFalse(text.equals(encryptedText));
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        assertEquals(text, decryptedText);
    }

    @Test
    public void testEncryptionXML() throws Exception {
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                      "<inventory>" +
                      "  <item name=\"item1\">Desk</item>" +
                      "  <item name=\"item2\">Chair</item>" +
                      "</inventory>";
        String encryptedText = encryptionUtil.encrypt(text);
        assertFalse(text.equals(encryptedText));
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        assertEquals(text, decryptedText);
    }

}
