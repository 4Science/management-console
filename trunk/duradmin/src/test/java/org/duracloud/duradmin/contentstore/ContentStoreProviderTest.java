
package org.duracloud.duradmin.contentstore;

import org.duracloud.error.ContentStoreException;
import org.junit.Assert;
import org.junit.Test;

public class ContentStoreProviderTest
        extends ContentStoreProviderTestBase {

    @Test
    public void testGetContentStoreSelector() throws ContentStoreException {
        String selectedId = this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(selectedId);

        String newSelectedId = "new-store-id";
        this.contentStoreProvider.setSelectedContentStoreId(newSelectedId);

        selectedId = this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(selectedId);
        Assert.assertEquals(newSelectedId, selectedId);
    }

    @Test
    public void testGetContentStore() throws Exception {
        Assert.assertNotNull(this.contentStoreProvider.getContentStore());
    }

}
