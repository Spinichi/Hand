package com.finger.hand_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MongoDB 설정
 * LocalDateTime과 LocalDate를 KST로 MongoDB에 저장하도록 설정
 *
 * MongoDB는 Date를 UTC로 저장하므로, 저장 시 +9시간, 조회 시 -9시간 처리
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new LocalDateTimeToDateKstConverter());
        converters.add(new DateToLocalDateTimeKstConverter());
        converters.add(new LocalDateToDateKstConverter());
        converters.add(new DateToLocalDateKstConverter());
        return new MongoCustomConversions(converters);
    }

    /**
     * LocalDateTime → Date 변환 (저장 시)
     * MongoDB가 -9시간 할 것을 대비해 +9시간 더해서 보냄
     */
    @WritingConverter
    static class LocalDateTimeToDateKstConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) {
            return Timestamp.valueOf(source.plusHours(9));
        }
    }

    /**
     * Date → LocalDateTime 변환 (조회 시)
     * MongoDB가 +9시간 한 것을 -9시간 빼서 원래 KST로 복원
     */
    @ReadingConverter
    static class DateToLocalDateTimeKstConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) {
            return source.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .minusHours(9);
        }
    }

    /**
     * LocalDate → Date 변환 (저장 시)
     * MongoDB가 -9시간 할 것을 대비해 +9시간 더해서 보냄
     */
    @WritingConverter
    static class LocalDateToDateKstConverter implements Converter<LocalDate, Date> {
        @Override
        public Date convert(LocalDate source) {
            // LocalDate를 해당 날짜의 00:00:00으로 변환 후 +9시간
            return Timestamp.valueOf(source.atStartOfDay().plusHours(9));
        }
    }

    /**
     * Date → LocalDate 변환 (조회 시)
     * MongoDB가 +9시간 한 것을 -9시간 빼서 원래 KST로 복원
     */
    @ReadingConverter
    static class DateToLocalDateKstConverter implements Converter<Date, LocalDate> {
        @Override
        public LocalDate convert(Date source) {
            return source.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .minusHours(9)
                    .toLocalDate();
        }
    }
}
