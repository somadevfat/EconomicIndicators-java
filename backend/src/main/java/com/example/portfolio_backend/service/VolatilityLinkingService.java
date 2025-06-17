package com.example.portfolio_backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.entity.VolatilityData;

@Service
public class VolatilityLinkingService {

    private static final DateTimeFormatter JST_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // projectbrief.md 2.1.3. 時間帯区分（UTC -> JST変換後に使用）
    // 07-09時（JST 16-18時） -> UTC 07:00 - 09:00
    // 09-12時（JST 18-21時） -> UTC 09:00 - 12:00
    // 12-16時（JST 21-01時） -> UTC 12:00 - 16:00
    // 16-21時（JST 01-06時） -> UTC 16:00 - 21:00
    // 21-24時（JST 06-09時） -> UTC 21:00 - 00:00 (翌日)
    // 00-03時（JST 09-12時） -> UTC 00:00 - 03:00
    // 03-07時（JST 12-16時） -> UTC 03:00 - 07:00
    private static final Map<String, String> JST_TO_UTC_TIMESLOT_MAP = Map.ofEntries(
            Map.entry("16-18", "07-09"),
            Map.entry("18-21", "09-12"),
            Map.entry("21-01", "12-16"), // JST 21時 - 翌日1時
            Map.entry("01-06", "16-21"), // JST 翌日1時 - 翌日6時
            Map.entry("06-09", "21-00"), // JST 翌日6時 - 翌日9時 (projectbriefでは21-24だが、00の方が扱いやすい)
            Map.entry("09-12", "00-03"), // JST 翌日9時 - 翌日12時
            Map.entry("12-16", "03-07")  // JST 翌日12時 - 翌日16時
    );


    /**
     * 指定された経済指標リストとボラティリティデータリストを時間帯ベースで紐付ける。
     *
     * @param indicators 経済指標リスト
     * @param allVolatilityData ボラティリティデータリスト
     * @return 経済指標ごとの紐付けられたボラティリティデータのマッピング（紐付けられなかった場合はnull）
     */
    public Map<EconomicIndicator, VolatilityData> linkIndicatorsToVolatility(
            List<EconomicIndicator> indicators, List<VolatilityData> allVolatilityData) {
        
        Map<EconomicIndicator, VolatilityData> result = new HashMap<>();
        if (indicators == null || allVolatilityData == null) {
            return result;
        }

        Map<LocalDate, List<VolatilityData>> volatilityByDate = allVolatilityData.stream()
                .collect(Collectors.groupingBy(VolatilityData::getDate));

        for (EconomicIndicator indicator : indicators) {
            LocalDateTime jstTime = indicator.getJstTime();
            if (jstTime == null) {
                result.put(indicator, null);
                continue;
            }

            String utcTimeSlot = determineUtcTimeSlot(jstTime);
            if (utcTimeSlot == null) {
                result.put(indicator, null);
                continue;
            }
            
            // JSTの日付を基準にボラティリティデータを検索
            LocalDate indicatorJstDate = jstTime.toLocalDate();
            List<VolatilityData> dailyVolatility = volatilityByDate.getOrDefault(indicatorJstDate, Collections.emptyList());
            
            // JSTの21時以降で、UTCのtimeSlotが日にちをまたぐ場合、経済指標のJST日付の翌日のボラティリティも検索対象とする
            if (jstTime.getHour() >= 21 && (utcTimeSlot.equals("12-16") || utcTimeSlot.equals("16-21") || utcTimeSlot.equals("21-00"))) {
                 List<VolatilityData> nextDayVolatility = volatilityByDate.getOrDefault(indicatorJstDate.plusDays(1), Collections.emptyList());
                 dailyVolatility.addAll(nextDayVolatility);
            }


            Optional<VolatilityData> matchedVolatility = findVolatilityByTimeSlot(
                    dailyVolatility, utcTimeSlot, indicatorJstDate, indicator.getJstTime().toLocalTime());

            result.put(indicator, matchedVolatility.orElse(null));
        }
        return result;
    }

    /**
     * JST時刻から該当するUTC時間帯区分を特定する。
     *
     * @param jstTime JST時刻
     * @return UTC時間帯文字列 (例: "07-09")。該当なしの場合はnull。
     */
    private String determineUtcTimeSlot(LocalDateTime jstTime) {
        int hour = jstTime.getHour();
        // projectbrief.md 2.1.3 のJST時間帯と突き合わせる
        if (hour >= 16 && hour < 18) return JST_TO_UTC_TIMESLOT_MAP.get("16-18"); // JST 16-18時 -> UTC 07-09
        if (hour >= 18 && hour < 21) return JST_TO_UTC_TIMESLOT_MAP.get("18-21"); // JST 18-21時 -> UTC 09-12
        if (hour >= 21) return JST_TO_UTC_TIMESLOT_MAP.get("21-01"); // JST 21-翌日1時 -> UTC 12-16
        // 翌日の時間帯
        if (hour >= 1 && hour < 6) return JST_TO_UTC_TIMESLOT_MAP.get("01-06");   // JST 1-6時 -> UTC 16-21 (前日の16-21) -> 修正: JST 1-6 (翌日) -> UTC 16-21
        if (hour >= 6 && hour < 9) return JST_TO_UTC_TIMESLOT_MAP.get("06-09");   // JST 6-9時 -> UTC 21-00 (前日の21-24) -> 修正: JST 6-9 (翌日) -> UTC 21-00
        if (hour >= 9 && hour < 12) return JST_TO_UTC_TIMESLOT_MAP.get("09-12");  // JST 9-12時 -> UTC 00-03
        if (hour >= 12 && hour < 16) return JST_TO_UTC_TIMESLOT_MAP.get("12-16"); // JST 12-16時 -> UTC 03-07
        
        // JST 0時台の考慮: 21-01 の範囲に含まれる
        if (hour == 0) return JST_TO_UTC_TIMESLOT_MAP.get("21-01");


        return null; // どの時間帯にも該当しない
    }

    /**
     * 指定された日付とUTC時間帯に一致するボラティリティデータを検索する。
     * 複数の候補がある場合は、経済指標の時刻に最も近いものを返す。
     *
     * @param volatilities ボラティリティデータのリスト（特定の日付または連続する日付）
     * @param targetUtcTimeSlot 検索対象のUTC時間帯文字列 (例: "07-09")
     * @param indicatorJstDate 経済指標のJST日付
     * @param indicatorJstTime 経済指標のJST時刻
     * @return 一致するVolatilityDataのOptional。見つからない場合はOptional.empty()。
     */
    private Optional<VolatilityData> findVolatilityByTimeSlot(
        List<VolatilityData> volatilities, String targetUtcTimeSlot, LocalDate indicatorJstDate, LocalTime indicatorJstTime) {
        if (volatilities == null || targetUtcTimeSlot == null) {
            return Optional.empty();
        }

        ZoneId jstZone = ZoneId.of("Asia/Tokyo");
        ZoneId utcZone = ZoneId.of("UTC");

        return volatilities.stream()
            .filter(vd -> vd.getTimeSlot() != null && vd.getTimeSlot().equals(targetUtcTimeSlot))
            .min((v1, v2) -> {
                // ボラティリティデータの時間帯の中心時刻を計算して比較する
                // timeSlotが "HH-HH" 形式であることを前提とする
                LocalTime center1 = calculateCenterTime(v1.getTimeSlot());
                LocalTime center2 = calculateCenterTime(v2.getTimeSlot());
                
                // ボラティリティデータの日付と時間帯の中心からZonedDateTimeを作成(UTC)
                ZonedDateTime zdt1 = ZonedDateTime.of(v1.getDate(), center1, utcZone);
                ZonedDateTime zdt2 = ZonedDateTime.of(v2.getDate(), center2, utcZone);

                // 経済指標のJST時刻をZonedDateTimeとして作成
                ZonedDateTime indicatorZdt = ZonedDateTime.of(indicatorJstDate, indicatorJstTime, jstZone);

                long diff1 = Duration.between(indicatorZdt, zdt1.withZoneSameInstant(jstZone)).abs().toMinutes();
                long diff2 = Duration.between(indicatorZdt, zdt2.withZoneSameInstant(jstZone)).abs().toMinutes();
                
                return Long.compare(diff1, diff2);
            });
    }

    private LocalTime calculateCenterTime(String timeSlot) {
        // timeSlot "HH-HH" から中心時刻を計算
        try {
            String[] parts = timeSlot.split("-");
            int startHour = Integer.parseInt(parts[0]);
            int endHour = Integer.parseInt(parts[1]);
            if (endHour == 0) endHour = 24; // 00時を24時として扱う

            int centerHour = (startHour + endHour) / 2;
            int centerMinute = ((startHour * 60 + endHour * 60) / 2) % 60;
            
            return LocalTime.of(centerHour % 24, centerMinute);
        } catch (Exception e) {
            // パースエラー時はスロットの開始時刻を返す
            try {
                return LocalTime.parse(timeSlot.substring(0,2) + ":00");
            } catch (Exception ex) {
                return LocalTime.MIDNIGHT; // どうしようもなければ真夜中
            }
        }
    }

    // 元のメソッドは削除またはコメントアウト（新しいメソッドで置き換え）
    /*
    public Map<EconomicIndicator, VolatilityData> linkByTimeSlot(List<EconomicIndicator> indicators, List<VolatilityData> data) {
        // サーバータイムゾーン（キプロス標準時／夏時間）
        ZoneId serverZone = ZoneId.of("Europe/Nicosia");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // 日付ごとのデータグルーピング
        var dataByDate = data.stream()
            .collect(Collectors.groupingBy(VolatilityData::getDate));
        Map<EconomicIndicator, VolatilityData> result = new HashMap<>();
        for (EconomicIndicator indicator : indicators) {
            // JST → サーバー時間に変換
            ZonedDateTime indServerZdt = indicator.getJstTime()
                .atZone(ZoneId.of("Asia/Tokyo"))
                .withZoneSameInstant(serverZone);
            LocalDate indDate = indServerZdt.toLocalDate();
            var dailyData = dataByDate.getOrDefault(indDate, Collections.emptyList());
            if (dailyData.isEmpty()) continue;
            // 時刻ソート済みリスト作成
            var sorted = dailyData.stream()
                .map(d -> new AbstractMap.SimpleEntry<>(
                    LocalTime.parse(d.getTimeSlot(), timeFormatter), d)) // ここが問題。timeSlotは "HH-HH"
                .sorted(Entry.comparingByKey())
                .collect(Collectors.toList());
            var times = sorted.stream()
                .map(Entry::getKey)
                .collect(Collectors.toList());
            LocalTime targetTime = indServerZdt.toLocalTime();
            // 二分探索でインデックス取得
            int idx = Collections.binarySearch(times, targetTime);
            if (idx < 0) idx = -idx - 1;
            int nearestIdx;
            if (idx == 0) {
                nearestIdx = 0;
            } else if (idx >= times.size()) {
                nearestIdx = times.size() - 1;
            } else {
                long diffPrev = Duration.between(times.get(idx - 1), targetTime).abs().toMinutes();
                long diffNext = Duration.between(times.get(idx), targetTime).abs().toMinutes();
                nearestIdx = diffPrev <= diffNext ? idx - 1 : idx;
            }
            VolatilityData nearestData = sorted.get(nearestIdx).getValue();
            result.put(indicator, nearestData);
        }
        return result;
    }
    */
} 