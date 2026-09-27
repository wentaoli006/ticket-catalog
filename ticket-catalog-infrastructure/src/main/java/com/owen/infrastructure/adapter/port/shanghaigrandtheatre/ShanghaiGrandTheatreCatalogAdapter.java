package com.owen.infrastructure.adapter.port.shanghaigrandtheatre;

import com.owen.domain.supplier.adapter.port.ISupplierCatalogPort;
import com.owen.domain.supplier.model.valobj.RawProject;
import com.owen.domain.supplier.model.valobj.RawSession;
import com.owen.domain.supplier.model.valobj.RawTicketItem;
import com.owen.types.dp.Money;
import com.owen.types.enums.SaleStatusEnum;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.exception.AppException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 上海大剧院原始数据适配。协议字段到这里为止，领域只看值对象。
 */
@Component
public class ShanghaiGrandTheatreCatalogAdapter implements ISupplierCatalogPort {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final String DATA_ERROR = "SHANGHAI_GRAND_THEATRE_DATA";

    private final ShanghaiGrandTheatreClient client;

    public ShanghaiGrandTheatreCatalogAdapter(ShanghaiGrandTheatreClient client) {
        this.client = client;
    }

    @Override
    public SupplierEnum supplier() {
        return SupplierEnum.SHANGHAI_GRAND_THEATRE;
    }

    @Override
    public List<RawProject> listProjects() {
        List<RawProject> projects = new ArrayList<>();
        for (ShanghaiGrandTheatreModels.Program program : client.listPrograms()) {
            projects.add(toProject(program));
        }
        return projects;
    }

    @Override
    public List<RawSession> listSessions(String supplierProjectId, boolean seatSelection) {
        List<RawSession> sessions = new ArrayList<>();
        for (ShanghaiGrandTheatreModels.Schedule schedule : client.listSchedules(numberId(supplierProjectId, "项目ID"))) {
            sessions.add(toSession(schedule, seatSelection));
        }
        return sessions;
    }

    @Override
    public List<RawTicketItem> listTicketItems(String supplierSessionId, int maxSaleQty) {
        int max = Math.max(maxSaleQty, 1);
        List<RawTicketItem> items = new ArrayList<>();
        for (ShanghaiGrandTheatreModels.Price price : client.listPrices(numberId(supplierSessionId, "场次ID"))) {
            items.add(toTicketItem(price, max));
        }
        return items;
    }

    private static RawProject toProject(ShanghaiGrandTheatreModels.Program program) {
        // 项目报文没有实名字段，实名以场次 checkcard 为准，项目级先记否。
        return new RawProject(
                idOf(program.getId(), "项目ID"),
                required(program.getCnName(), "项目名称"),
                null,
                null,
                yes(program.getStatus()) ? SaleStatusEnum.ON_SALE : SaleStatusEnum.OFF_SALE,
                false,
                yes(program.getSupportSeat()),
                ZONE,
                firstText(program.getVenueName(), program.getStadiumName()),
                program.getStadiumAddress()
        );
    }

    private static RawSession toSession(ShanghaiGrandTheatreModels.Schedule schedule, boolean seatSelection) {
        String startText = required(schedule.getPlayTime(), "场次开始时间");
        boolean onSale = yes(schedule.getStatus()) && yes(schedule.getDisplay());
        int maxSaleQty = schedule.getMaxBuyLimit() == null || schedule.getMaxBuyLimit() < 1 ? 1 : schedule.getMaxBuyLimit();
        return new RawSession(
                idOf(schedule.getId(), "场次ID"),
                firstText(schedule.getCnName(), startText),
                time(startText, "场次开始时间"),
                time(schedule.getPlayEndTime(), "场次结束时间"),
                time(schedule.getOpentime(), "开售时间"),
                time(schedule.getClosetime(), "停售时间"),
                yes(schedule.getCheckcard()),
                seatSelection,
                onSale ? SaleStatusEnum.ON_SALE : SaleStatusEnum.OFF_SALE,
                maxSaleQty
        );
    }

    private static RawTicketItem toTicketItem(ShanghaiGrandTheatreModels.Price price, int maxSaleQty) {
        ShanghaiGrandTheatreModels.PriceInfo info = price.getPriceInfo();
        if (info == null || info.getPrice() == null) {
            throw new AppException(DATA_ERROR, "票品价格不能为空");
        }
        String ticketId = idOf(price.getId() != null ? price.getId() : price.getTicketPriceId(), "票品ID");
        int personCount = info.getUnitnum() == null || info.getUnitnum() < 1 ? 1 : info.getUnitnum();
        String plu = price.getTicketPriceId() == null ? ticketId : String.valueOf(price.getTicketPriceId());
        return new RawTicketItem(
                ticketId,
                firstText(info.getCnName(), info.getDescription(), info.getRemark(), ticketId),
                firstText(info.getRemark(), info.getDescription()),
                1,
                maxSaleQty,
                Money.cny(info.getPrice()),
                0,
                plu,
                SaleStatusEnum.ON_SALE,
                personCount
        );
    }

    private static boolean yes(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private static String idOf(Long id, String field) {
        if (id == null) {
            throw new AppException(DATA_ERROR, field + "不能为空");
        }
        return String.valueOf(id);
    }

    private static Long numberId(String id, String field) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new AppException(DATA_ERROR, field + "不是数字: " + id, exception);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new AppException(DATA_ERROR, field + "不能为空");
        }
        return value.trim();
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static LocalDateTime time(String text, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim(), TIME);
        } catch (DateTimeParseException exception) {
            throw new AppException(DATA_ERROR, field + "无法解析: " + text, exception);
        }
    }

}
