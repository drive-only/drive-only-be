package drive_only.drive_only_server.dto.report;

public record ReportResponse(
        String message,
        boolean hidden,   // 현재 상태: 숨김=true, 해제=false
        boolean created   // 이번 요청으로 새로 생성되었으면 true (POST만 의미 있음)
) {}
