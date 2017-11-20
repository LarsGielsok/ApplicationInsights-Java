/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.web.internal.correlation;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

public class CdsProfileFetcherTests {
    
    @Test
    public void testFetchApplicationId() throws InterruptedException, ExecutionException {

        //setup
        MockHttpAsyncClientWrapper clientWrapper = new MockHttpAsyncClientWrapper();
        clientWrapper.setAppId("AppId");
        CdsProfileFetcher.INSTANCE.setHttpClient(clientWrapper.getClient());

        // the first time we try to fetch the profile, we should get a "pending" task status
        // since the profile fetcher uses asynchronous calls to retrieve the profile from CDS
        ProfileFetcherResult result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.PENDING, result.getStatus());
        Assert.assertNull(result.getAppId());

        // our mock http client returns immediately, so a second call to fetch should get us the
        // result of that task started in the previous call.
        result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.COMPLETE, result.getStatus());
        Assert.assertEquals("AppId", result.getAppId());
    }

    @Test
    public void testFetchApplicationIdMultipleIkeys() throws InterruptedException, ExecutionException {

        //setup
        MockHttpAsyncClientWrapper clientWrapper = new MockHttpAsyncClientWrapper();
        clientWrapper.setAppId("AppId");
        CdsProfileFetcher.INSTANCE.setHttpClient(clientWrapper.getClient());

        // the first time we try to fetch the profile, we should get a "pending" task status
        // since the profile fetcher uses asynchronous calls to retrieve the profile from CDS
        ProfileFetcherResult result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.PENDING, result.getStatus());
        Assert.assertNull(result.getAppId());

        // call for a second ikey, should also return "pending"
        result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey2");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.PENDING, result.getStatus());
        Assert.assertNull(result.getAppId());

        // our mock http client returns immediately, so a second call to fetch should get us the
        // result of that task started in the previous call.
        result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.COMPLETE, result.getStatus());
        Assert.assertEquals("AppId", result.getAppId());

        clientWrapper.setAppId("AppId2");
        result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey2");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.COMPLETE, result.getStatus());
        Assert.assertEquals("AppId2", result.getAppId());
    }

    @Test(expected = ExecutionException.class)
    public void testFetchApplicationIdFailure() throws InterruptedException, ExecutionException {

        //setup - mimic timeout from the async http call
        MockHttpAsyncClientWrapper clientWrapper = new MockHttpAsyncClientWrapper();
        clientWrapper.setAppId("AppId");
        clientWrapper.setFailureOn(true);
        CdsProfileFetcher.INSTANCE.setHttpClient(clientWrapper.getClient());

        // the first time we try to fetch the profile, we should get a "pending" task status
        // since the profile fetcher uses asynchronous calls to retrieve the profile from CDS
        ProfileFetcherResult result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.PENDING, result.getStatus());
        Assert.assertNull(result.getAppId());

        // our mock http client has been instructed to fail
        result = CdsProfileFetcher.INSTANCE.fetchAppProfile("ikey");
        Assert.assertEquals(ProfileFetcherResultTaskStatus.FAILED, result.getStatus());
    }
}