/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.datalakeanalytics.v2016_11_01.implementation;

import com.microsoft.azure.management.datalakeanalytics.v2016_11_01.StorageContainer;
import com.microsoft.azure.arm.model.implementation.IndexableRefreshableWrapperImpl;
import rx.Observable;
import org.joda.time.DateTime;

class StorageContainerImpl extends IndexableRefreshableWrapperImpl<StorageContainer, StorageContainerInner> implements StorageContainer {
    private final DataLakeAnalyticsManager manager;
    private String resourceGroupName;
    private String accountName;
    private String storageAccountName;
    private String containerName;

    StorageContainerImpl(StorageContainerInner inner,  DataLakeAnalyticsManager manager) {
        super(null, inner);
        this.manager = manager;
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.id(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.id(), "accounts");
        this.storageAccountName = IdParsingUtils.getValueFromIdByName(inner.id(), "storageAccounts");
        this.containerName = IdParsingUtils.getValueFromIdByName(inner.id(), "containers");
    }

    @Override
    public DataLakeAnalyticsManager manager() {
        return this.manager;
    }

    @Override
    protected Observable<StorageContainerInner> getInnerAsync() {
        StorageAccountsInner client = this.manager().inner().storageAccounts();
        return client.getStorageContainerAsync(this.resourceGroupName, this.accountName, this.storageAccountName, this.containerName);
    }



    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public DateTime lastModifiedTime() {
        return this.inner().lastModifiedTime();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

}
