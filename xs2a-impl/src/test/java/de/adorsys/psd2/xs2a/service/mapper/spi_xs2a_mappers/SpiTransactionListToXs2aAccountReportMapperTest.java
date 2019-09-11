package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class SpiTransactionListToXs2aAccountReportMapperTest {
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;

    @InjectMocks
    private SpiTransactionListToXs2aAccountReportMapper spiTransactionListToXs2aAccountReportMapper;


    @Test
    public void mapToXs2aAccountReport_shouldReturnEmptyOptional() {
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOOKED, null, null);
        Assertions.assertThat(accountReport).isEqualTo(Optional.empty());
    }

    @Test
    public void mapToXs2aAccountReport_shouldReturnOptionalContaingRawTransactions() {
        byte[] rawTransactions = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOOKED, null, rawTransactions);
        Assertions.assertThat(accountReport).isEqualTo(Optional.of(new Xs2aAccountReport(null, null, rawTransactions)));
    }

    @Test
    public void mapToXs2aAccountReport_shouldReturnOnlyPending() {
        List<SpiTransaction> transactions = jsonReader.getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.PENDING, transactions, null);

        Assertions.assertThat(accountReport).isNotNull();
        Assertions.assertThat(accountReport.isPresent()).isTrue();

        Assertions.assertThat(accountReport.get().getBooked()).isEqualTo(Collections.emptyList());
        Assertions.assertThat(accountReport.get().getPending().size()).isEqualTo(1);
    }

    @Test
    public void mapToXs2aAccountReport_shouldReturnOnlyBooked() {
        List<SpiTransaction> transactions = jsonReader.getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOOKED, transactions, null);

        Assertions.assertThat(accountReport).isNotNull();
        Assertions.assertThat(accountReport.isPresent()).isTrue();


        Assertions.assertThat(accountReport.get().getBooked().size()).isEqualTo(1);
        Assertions.assertThat(accountReport.get().getPending()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void mapToXs2aAccountReport_shouldReturnBoth() {
        List<SpiTransaction> transactions = jsonReader.getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, transactions, null);

        Assertions.assertThat(accountReport).isNotNull();
        Assertions.assertThat(accountReport.isPresent()).isTrue();

        Assertions.assertThat(accountReport.get().getBooked().size()).isEqualTo(1);
        Assertions.assertThat(accountReport.get().getPending().size()).isEqualTo(1);
    }
}
