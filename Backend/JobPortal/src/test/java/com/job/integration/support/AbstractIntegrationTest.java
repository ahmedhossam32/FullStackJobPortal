package com.job.integration.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.dto.request.LoginRequestDTO;
import com.job.entity.JobSeeker;
import com.job.enums.JobType;
import com.job.enums.WorkMode;
import com.job.repository.ApplicationRepository;
import com.job.repository.EmployerRepository;
import com.job.repository.JobRepository;
import com.job.repository.JobSeekerRepository;
import com.job.repository.NotificationRepository;
import com.job.repository.UserRepository;
import com.job.service.impl.CloudinaryService;
import com.job.service.interfaces.EmailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for every full-stack integration test: real controllers, real security filters,
 * real services, real repositories and a real (containerized) Postgres, with only the two calls
 * that leave the process -- Cloudinary and outbound email -- replaced by mocks. Subclasses that
 * don't override any property sources share one Spring context and one Postgres container for
 * the whole run.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.getInstance();

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String DEFAULT_PASSWORD = "Password123";

    protected static final byte[] VALID_PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D
    };
    protected static final byte[] VALID_PDF_BYTES =
            "%PDF-1.4\n%fake pdf content for tests\n".getBytes(StandardCharsets.US_ASCII);

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected UserRepository userRepository;
    @Autowired protected JobSeekerRepository jobSeekerRepository;
    @Autowired protected EmployerRepository employerRepository;
    @Autowired protected JobRepository jobRepository;
    @Autowired protected ApplicationRepository applicationRepository;
    @Autowired protected NotificationRepository notificationRepository;

    @Value("${jwt.secret}")
    protected String jwtSecret;

    @MockitoBean
    protected CloudinaryService cloudinaryService;

    @MockitoBean
    protected EmailService emailService;

    @BeforeEach
    void resetExternalMocks() {
        Mockito.reset(cloudinaryService, emailService);
        Mockito.lenient().when(cloudinaryService.uploadImage(any()))
                .thenReturn("https://res.cloudinary.test/image/upload/fake-image.png");
        Mockito.lenient().when(cloudinaryService.uploadResume(any()))
                .thenReturn("https://res.cloudinary.test/raw/upload/fake-resume.pdf");
    }

    // ── Identifiers ──────────────────────────────────────────────────────────

    protected static String uniqueUsername(String prefix) {
        return (prefix + "_" + SEQUENCE.incrementAndGet() + "_" + UUID.randomUUID().toString().substring(0, 6))
                .toLowerCase();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected record AuthedUser(String username, String password, String token, long id) {}

    // ── Signup / signin ─────────────────────────────────────────────────────

    protected AuthedUser createJobSeeker(String prefix) throws Exception {
        String username = uniqueUsername(prefix);
        JobSeekerRegisterRequestDTO dto = new JobSeekerRegisterRequestDTO(
                "Test Seeker " + prefix, username, DEFAULT_PASSWORD, LocalDate.of(1995, 1, 1), username + "@example.com");

        mockMvc.perform(post("/auth/signup/jobseeker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String token = signIn(username, DEFAULT_PASSWORD);
        long id = userRepository.findByUsername(username).orElseThrow().getId();
        return new AuthedUser(username, DEFAULT_PASSWORD, token, id);
    }

    protected AuthedUser createEmployer(String prefix) throws Exception {
        String username = uniqueUsername(prefix);
        EmployerRegisterRequestDTO dto = new EmployerRegisterRequestDTO(
                "Test Employer " + prefix, username, DEFAULT_PASSWORD, "Acme " + prefix, username + "@example.com", "Technology");

        mockMvc.perform(post("/auth/signup/employer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String token = signIn(username, DEFAULT_PASSWORD);
        long id = userRepository.findByUsername(username).orElseThrow().getId();
        return new AuthedUser(username, DEFAULT_PASSWORD, token, id);
    }

    protected String signIn(String username, String password) throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO(username, password);

        MvcResult result = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    protected String expiredTokenFor(String username) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now - 7_200_000))
                .setExpiration(new Date(now - 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Jobs / applications / resumes ───────────────────────────────────────

    protected void giveResume(long jobSeekerId, String url) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId).orElseThrow();
        jobSeeker.setResumeUrl(url);
        jobSeekerRepository.save(jobSeeker);
    }

    protected String jobJson(String title) throws Exception {
        return jobJson(title, JobType.FULL_TIME, WorkMode.HYBRID, null);
    }

    protected String jobJson(String title, JobType type, WorkMode workMode) throws Exception {
        return jobJson(title, type, workMode, null);
    }

    protected String jobJson(String title, JobType type, WorkMode workMode, List<String> screeningQuestions)
            throws Exception {
        JobRequestDTO dto = new JobRequestDTO(
                title, "Description for " + title, "Cairo", type, workMode, null, null, screeningQuestions);
        return objectMapper.writeValueAsString(dto);
    }

    protected long createJob(String employerToken, String title) throws Exception {
        return createJob(employerToken, title, JobType.FULL_TIME, WorkMode.HYBRID);
    }

    protected long createJob(String employerToken, String title, JobType type, WorkMode workMode) throws Exception {
        MvcResult result = mockMvc.perform(post("/jobs")
                        .header(AUTH_HEADER, bearer(employerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(title, type, workMode)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("jobId").asLong();
    }

    protected long applyToJob(String seekerToken, long jobId) throws Exception {
        com.job.dto.request.ApplicationRequestDTO dto = new com.job.dto.request.ApplicationRequestDTO(jobId, null);
        String body = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/applications")
                        .header(AUTH_HEADER, bearer(seekerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("applicationId").asLong();
    }

    // ── Multipart fixtures ───────────────────────────────────────────────────

    protected static MockMultipartFile pngPart() {
        return new MockMultipartFile("file", "photo.png", "image/png", VALID_PNG_BYTES);
    }

    protected static MockMultipartFile pdfPart() {
        return new MockMultipartFile("file", "resume.pdf", "application/pdf", VALID_PDF_BYTES);
    }
}
