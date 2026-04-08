package com.soen345.ticketing.android;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TicketingDataProviderThreadSafetyTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        AndroidTestSupport.resetTicketingDataProvider();
    }

    @Test
    public void concurrentInitializationReturnsSingleInstances() throws InterruptedException {
        int workerCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(workerCount);

        Set<Object> eventRepos = Collections.synchronizedSet(new HashSet<>());
        Set<Object> reservationRepos = Collections.synchronizedSet(new HashSet<>());
        Set<Object> confirmationServices = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < workerCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    eventRepos.add(TicketingDataProvider.eventRepository(context));
                    reservationRepos.add(TicketingDataProvider.reservationRepository(context));
                    confirmationServices.add(TicketingDataProvider.confirmationService(context));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdownNow();

        assertEquals(1, eventRepos.size());
        assertEquals(1, reservationRepos.size());
        assertEquals(1, confirmationServices.size());
    }
}
