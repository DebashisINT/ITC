package com.breezefsmdsm.features.document.api

import com.breezefsmdsm.features.dymanicSection.api.DynamicApi
import com.breezefsmdsm.features.dymanicSection.api.DynamicRepo

object DocumentRepoProvider {
    fun documentRepoProvider(): DocumentRepo {
        return DocumentRepo(DocumentApi.create())
    }

    fun documentRepoProviderMultipart(): DocumentRepo {
        return DocumentRepo(DocumentApi.createImage())
    }
}