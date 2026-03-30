package com.soen345.ticketing.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirebaseAvailabilityTest {
    @Test
    public void firestoreIsReachableAndRespondsWithinThreshold()
            throws ExecutionException, InterruptedException, TimeoutException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long start = System.currentTimeMillis();

        // Attempt to read from Firestore
        Tasks.await(
                db.collection("events").limit(1).get(),
                5, TimeUnit.SECONDS
        );

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Firestore response time: " + elapsed + "ms");

        // Should respond within 5 seconds
        assertTrue("Firestore took too long: " + elapsed + "ms", elapsed < 5000);
    }

    @Test
    public void firestoreWriteAndReadRoundTripSucceeds()
            throws ExecutionException, InterruptedException, TimeoutException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        java.util.Map<String, Object> testDoc = new java.util.HashMap<>();
        testDoc.put("test", true);
        testDoc.put("timestamp", System.currentTimeMillis());

        // Write
        Tasks.await(
                db.collection("_healthcheck").document("test").set(testDoc),
                5, TimeUnit.SECONDS
        );

        // Read back
        com.google.firebase.firestore.DocumentSnapshot result = Tasks.await(
                db.collection("_healthcheck").document("test").get(),
                5, TimeUnit.SECONDS
        );

        assertTrue("Document should exist after write", result.exists());
        assertTrue("Document should contain test flag",
                Boolean.TRUE.equals(result.getBoolean("test")));
    }
}
