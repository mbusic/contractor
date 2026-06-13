package hr.qnr.contractor.config;

import hr.qnr.contractor.entity.*;
import hr.qnr.contractor.entity.Order.Status;
import hr.qnr.contractor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final BranchRepository branchRepo;
    private final ClientRepository clientRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final OrderSequenceRepository sequenceRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (branchRepo.count() > 0) {
            return; // already seeded
        }

        // --- Branches ---
        Branch zagreb  = branchRepo.save(Branch.builder().name("Kricco Zagreb").city("Zagreb").build());
        Branch split   = branchRepo.save(Branch.builder().name("Kricco Split").city("Split").build());
        Branch zadar   = branchRepo.save(Branch.builder().name("Kricco Zadar").city("Zadar").build());
        Branch osijek  = branchRepo.save(Branch.builder().name("Kricco Osijek").city("Osijek").build());
        Branch pula    = branchRepo.save(Branch.builder().name("Kricco Pula").city("Pula").build());

        // --- Clients ---
        Client company = clientRepo.save(Client.builder()
                .type(Client.ClientType.COMPANY)
                .name("Petar Perić d.o.o.")
                .contactPerson("Petar Perić")
                .phone("097 587 6210")
                .email("petar.peric@example.hr")
                .build());

        Location loc1 = Location.builder()
                .client(company)
                .address("A.G. Matoša 42")
                .city("Zagreb 10000")
                .build();
        Location loc2 = Location.builder()
                .client(company)
                .address("Vukovarska 18")
                .city("Split 21000")
                .build();
        company.getLocations().addAll(List.of(loc1, loc2));
        clientRepo.save(company);

        Client individual = clientRepo.save(Client.builder()
                .type(Client.ClientType.INDIVIDUAL)
                .name("Ana Anić")
                .contactPerson("Ana Anić")
                .phone("091 234 5678")
                .email("ana.anic@example.hr")
                .address("Ilica 10, Zagreb 10000")
                .build());

        // --- Users ---
        userRepo.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .role(User.Role.ADMIN)
                .displayName("Administrator")
                .build());

        User officeUser = userRepo.save(User.builder()
                .username("office")
                .password(passwordEncoder.encode("office"))
                .role(User.Role.OFFICE)
                .displayName("Dispečer")
                .branch(zagreb)
                .build());

        User servicerUser = userRepo.save(User.builder()
                .username("servicer")
                .password(passwordEncoder.encode("servicer"))
                .role(User.Role.SERVICER)
                .displayName("Ivan Horvat")
                .branch(zagreb)
                .build());

        userRepo.save(User.builder()
                .username("client")
                .password(passwordEncoder.encode("client"))
                .role(User.Role.CLIENT)
                .displayName("Petar Perić")
                .client(company)
                .build());

        // --- Order sequence bootstrap (seed uses 001-005/25) ---
        sequenceRepo.save(OrderSequence.builder().year(2025).lastSequence(5).build());

        // --- Orders ---
        Order o1 = orderRepo.save(Order.builder()
                .orderNumber("001/25")
                .branch(zagreb)
                .client(company)
                .location("A.G. Matoša 42, Zagreb 10000")
                .contactPerson("Petar Perić")
                .phone("097 587 6210")
                .email("petar.peric@example.hr")
                .description("Oštećena keramika na ulazu")
                .urgency("1 dan")
                .status(Status.RESOLVED)
                .assignedServicer(servicerUser)
                .actualWorkHours(8.0)
                .actualNumberOfWorkers(3)
                .actualTotalHours(24.0)
                .actualKm(76)
                .actualMaterialCost(new BigDecimal("42.00"))
                .estimatedWorkHours(8.0)
                .estimatedNumberOfWorkers(3)
                .estimatedTotalHours(24.0)
                .estimatedKm(80)
                .estimatedMaterialCost(new BigDecimal("50.00"))
                .build());

        orderRepo.save(Order.builder()
                .orderNumber("002/25")
                .branch(split)
                .client(company)
                .location("Vukovarska 18, Split 21000")
                .contactPerson("Petar Perić")
                .phone("097 587 6210")
                .description("Kvar na instalaciji")
                .urgency("1 tjedan")
                .status(Status.IN_PROGRESS)
                .assignedServicer(servicerUser)
                .build());

        orderRepo.save(Order.builder()
                .orderNumber("003/25")
                .branch(zadar)
                .client(individual)
                .location("Ulica Stjepana Radića 5, Zadar 23000")
                .contactPerson("Ana Anić")
                .phone("091 234 5678")
                .description("Popravak klima uređaja")
                .urgency("1 mjesec")
                .status(Status.PENDING)
                .build());

        orderRepo.save(Order.builder()
                .orderNumber("004/25")
                .branch(osijek)
                .client(company)
                .location("Europska avenija 2, Osijek 31000")
                .contactPerson("Petar Perić")
                .phone("097 587 6210")
                .description("Hitna intervencija - curenje vode")
                .urgency("Isti dan")
                .status(Status.PENDING)
                .build());

        orderRepo.save(Order.builder()
                .orderNumber("005/25")
                .branch(pula)
                .client(individual)
                .location("Flanatička 14, Pula 52100")
                .contactPerson("Ana Anić")
                .phone("091 234 5678")
                .description("Redovno održavanje sustava grijanja")
                .urgency("6 mjeseci")
                .status(Status.PENDING)
                .build());
    }
}
