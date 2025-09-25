package com.mindshift.ums.service.adapter;

import com.mindshift.ums.domain.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service that manages and routes messages to appropriate channel adapters.
 */
@Service
public class ChannelAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelAdapterService.class);

    private final List<ChannelAdapter> channelAdapters;

    @Autowired
    public ChannelAdapterService(List<ChannelAdapter> channelAdapters) {
        this.channelAdapters = channelAdapters;
        logger.info("Initialized with {} channel adapters", channelAdapters.size());
        channelAdapters.forEach(adapter ->
            logger.info("Registered channel adapter: {}", adapter.getChannelType()));
    }

    /**
     * Send a message using the appropriate channel adapter.
     *
     * @param message The message to send
     * @return The result of the send operation
     */
    public ChannelAdapter.SendResult sendMessage(Message message) {
        logger.debug("Finding adapter for message: {} with channel: {}",
            message.getRequestId(), message.getChannel());

        Optional<ChannelAdapter> adapter = findAdapter(message);

        if (adapter.isEmpty()) {
            logger.error("No suitable adapter found for message: {} with channel: {}",
                message.getRequestId(), message.getChannel());
            return ChannelAdapter.SendResult.failure(
                "NO_ADAPTER",
                "No suitable channel adapter found for channel: " + message.getChannel()
            );
        }

        try {
            ChannelAdapter channelAdapter = adapter.get();
            logger.debug("Using adapter {} for message: {}",
                channelAdapter.getChannelType(), message.getRequestId());

            return channelAdapter.send(message);

        } catch (Exception e) {
            logger.error("Adapter failed to send message: {}", message.getRequestId(), e);
            return ChannelAdapter.SendResult.failure("ADAPTER_ERROR", e.getMessage());
        }
    }

    /**
     * Find the appropriate adapter for a message.
     *
     * @param message The message to find an adapter for
     * @return Optional containing the adapter if found
     */
    private Optional<ChannelAdapter> findAdapter(Message message) {
        return channelAdapters.stream()
            .filter(adapter -> adapter.canHandle(message))
            .findFirst();
    }

    /**
     * Get all available channel types.
     *
     * @return List of channel type names
     */
    public List<String> getAvailableChannels() {
        return channelAdapters.stream()
            .map(ChannelAdapter::getChannelType)
            .toList();
    }

    /**
     * Check if a specific channel is supported.
     *
     * @param channelType The channel type to check
     * @return true if the channel is supported
     */
    public boolean isChannelSupported(String channelType) {
        return channelAdapters.stream()
            .anyMatch(adapter -> adapter.getChannelType().equals(channelType));
    }
}