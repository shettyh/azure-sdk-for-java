/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.datalakeanalytics.v2015_10_01_preview;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.datalakeanalytics.v2015_10_01_preview.implementation.BlobContainerInner;
import com.microsoft.azure.arm.model.Indexable;
import com.microsoft.azure.arm.model.Refreshable;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.datalakeanalytics.v2015_10_01_preview.implementation.DataLakeAnalyticsManager;

/**
 * Type representing BlobContainer.
 */
public interface BlobContainer extends HasInner<BlobContainerInner>, Indexable, Refreshable<BlobContainer>, HasManager<DataLakeAnalyticsManager> {
    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the properties value.
     */
    BlobContainerProperties properties();

    /**
     * @return the type value.
     */
    String type();

}
