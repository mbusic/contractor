package hr.qnr.contractor.service;

import hr.qnr.contractor.entity.OrderSequence;
import hr.qnr.contractor.repository.OrderSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderSequenceRepository sequenceRepository;

    @Transactional
    public String next() {
        int year = LocalDate.now().getYear();
        int twoDigitYear = year % 100;

        OrderSequence seq = sequenceRepository.findById(year)
                .orElseGet(() -> OrderSequence.builder().year(year).lastSequence(0).build());

        seq.setLastSequence(seq.getLastSequence() + 1);
        sequenceRepository.save(seq);

        return String.format("%03d/%02d", seq.getLastSequence(), twoDigitYear);
    }
}
