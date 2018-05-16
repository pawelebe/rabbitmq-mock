package com.github.fridujo.rabbitmq.mock;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ChannelTest {

    @Test
    void close_closes_channel() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                channel.close();
                assertThat(channel.isOpen()).isFalse();
            }
        }
    }

    @Test
    void abort_closes_channel() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                channel.abort();
                assertThat(channel.isOpen()).isFalse();
            }
        }
    }

    @Test
    void channel_number_can_be_accessed() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            int channelNumber = new Random().nextInt();
            try (Channel channel = conn.createChannel(channelNumber)) {
                assertThat(channel.getChannelNumber()).isEqualTo(channelNumber);
            }
        }
    }

    @Test
    void getConnection_returns_the_actual_connection_which_created_the_channel() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                assertThat(channel.getConnection()).isEqualTo(conn);
            }
        }
    }

    @Test
    void basicQos_does_not_throw() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                channel.basicQos(30);
            }
        }
    }

    @Test
    void exchangeDeclare_creates_it() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                assertThat(channel.exchangeDeclare("test1", "fanout")).isNotNull();
                assertThat(channel.exchangeDeclarePassive("test1")).isNotNull();
            }
        }
    }

    @Test
    void exchangeDeclareNoWait_creates_it() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                channel.exchangeDeclareNoWait("test1", BuiltinExchangeType.DIRECT, true, false, false, null);
                assertThat(channel.exchangeDeclarePassive("test1")).isNotNull();
            }
        }
    }

    @Test
    void exchangeDeclarePassive_throws_when_not_existing() throws IOException, TimeoutException {
        try (Connection conn = new MockConnectionFactory().newConnection()) {
            try (Channel channel = conn.createChannel()) {
                assertThatExceptionOfType(IOException.class)
                    .isThrownBy(() -> channel.exchangeDeclarePassive("test1"))
                    .withMessage("com.rabbitmq.client.ShutdownSignalException: no exchange 'test1' in vhost '/' channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND, class-id=40, method-id=10)")
                    .withCauseExactlyInstanceOf(ShutdownSignalException.class);
            }
        }
    }
}