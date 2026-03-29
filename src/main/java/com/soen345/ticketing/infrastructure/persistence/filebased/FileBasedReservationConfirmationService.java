package com.soen345.ticketing.infrastructure.persistence.filebased;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FileBasedReservationConfirmationService implements ReservationConfirmationService {
    private final Path storageDirectory;
    private final Map<UUID, ReservationConfirmation> confirmationCache;

    public FileBasedReservationConfirmationService(String storagePath) {
        this.storageDirectory = Paths.get(storagePath);
        this.confirmationCache = new HashMap<>();
        initializeStorage();
        loadAllConfirmations();
    }

    @Override
    public void saveConfirmation(ReservationConfirmation confirmation) {
        Objects.requireNonNull(confirmation, "confirmation must not be null");

        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(storageDirectory);

            // Save to file
            Path confirmationFile = storageDirectory.resolve(
                    confirmation.reservationId().toString() + ".confirmation"
            );

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(confirmationFile.toFile()))) {
                oos.writeObject(confirmation);
            }

            // Update cache
            confirmationCache.put(confirmation.reservationId(), confirmation);

        } catch (IOException e) {
            throw new ReservationConfirmationStorageException(
                    "Failed to save confirmation for reservation " + confirmation.reservationId(), e
            );
        }
    }

    @Override
    public Optional<ReservationConfirmation> getConfirmation(UUID reservationId) {
        Objects.requireNonNull(reservationId, "reservationId must not be null");

        // Check cache first
        if (confirmationCache.containsKey(reservationId)) {
            return Optional.of(confirmationCache.get(reservationId));
        }

        // Try to load from file
        try {
            Path confirmationFile = storageDirectory.resolve(
                    reservationId.toString() + ".confirmation"
            );

            if (!Files.exists(confirmationFile)) {
                return Optional.empty();
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(confirmationFile.toFile()))) {
                ReservationConfirmation confirmation = (ReservationConfirmation) ois.readObject();
                confirmationCache.put(reservationId, confirmation);
                return Optional.of(confirmation);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new ReservationConfirmationStorageException(
                    "Failed to retrieve confirmation for reservation " + reservationId, e
            );
        }
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new ReservationConfirmationStorageException(
                    "Failed to initialize storage directory: " + storageDirectory, e
            );
        }
    }

    private void loadAllConfirmations() {
        try {
            if (!Files.exists(storageDirectory)) {
                return;
            }

            Files.list(storageDirectory)
                    .filter(path -> path.getFileName().toString().endsWith(".confirmation"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(
                                new FileInputStream(path.toFile()))) {
                            ReservationConfirmation confirmation = (ReservationConfirmation) ois.readObject();
                            confirmationCache.put(confirmation.reservationId(), confirmation);
                        } catch (IOException | ClassNotFoundException e) {
                            // Log and skip corrupted files
                            System.err.println("Failed to load confirmation from " + path + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new ReservationConfirmationStorageException(
                    "Failed to load confirmations from storage", e
            );
        }
    }
}
