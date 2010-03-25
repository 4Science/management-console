package org.duracloud.sync;

import org.duracloud.sync.mgmt.ChangedList;
import org.junit.Before;
import org.junit.After;
import static junit.framework.Assert.assertNull;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class SyncTestBase {
    
    protected ChangedList changedList;

    @Before
    public void setUp() throws Exception {
        changedList = ChangedList.getInstance();
        assertNull(changedList.getChangedFile());
    }

    @After
    public void tearDown() throws Exception {
        while(changedList.getChangedFile() != null) {}
        assertNull(changedList.getChangedFile());
    }

    protected File createTempDir(String dirName) {
        File tempDir = new File(System.getProperty("java.io.tmpdir") +
                           File.pathSeparator + dirName);
        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
    }
}
