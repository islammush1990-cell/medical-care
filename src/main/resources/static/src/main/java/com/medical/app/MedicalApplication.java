package com.medical.app;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
public class MedicalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalApplication.class, args);
    }
}

@Entity
class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    // بيانات الاستقبال
    public String patientName;
    public String nationalId;
    public String clinic;
    
    // بيانات العيادة والمختبر والصيدلية
    public String diagnosis = "بانتظار الطبيب";
    public String labTest = "لا يوجد";
    public String labResult = "قيد الانتظار";
    public String medicine = "لا يوجد";
    public String status = "في العيادة"; // في العيادة -> في المختبر -> في الصيدلية -> مكتمل
}

interface RecordRepository extends JpaRepository<MedicalRecord, Long> {}

@RestController
@RequestMapping("/api/records")
class MedicalController {
    private final RecordRepository repo;

    public MedicalController(RecordRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<MedicalRecord> getAll() {
        return repo.findAll();
    }

    // 1. الاستقبال: إضافة مريض جديد
    @PostMapping
    public MedicalRecord create(@RequestBody MedicalRecord record) {
        return repo.save(record);
    }

    // 2. العيادة: إضافة تشخيص وطلب تحليل أو دواء
    @PutMapping("/{id}/clinic")
    public MedicalRecord updateClinic(@PathVariable Long id, @RequestBody MedicalRecord data) {
        MedicalRecord r = repo.findById(id).orElseThrow();
        r.diagnosis = data.diagnosis;
        r.labTest = data.labTest;
        r.medicine = data.medicine;
        r.status = (!data.labTest.isEmpty() && !data.labTest.equals("لا يوجد")) ? "في المختبر" : "في الصيدلية";
        return repo.save(r);
    }

    // 3. المختبر: إدخال نتيجة التحليل
    @PutMapping("/{id}/lab")
    public MedicalRecord updateLab(@PathVariable Long id, @RequestBody MedicalRecord data) {
        MedicalRecord r = repo.findById(id).orElseThrow();
        r.labResult = data.labResult;
        r.status = "في الصيدلية";
        return repo.save(r);
    }

    // 4. الصيدلية: صرف الدواء وإغلاق الزيارة
    @PutMapping("/{id}/pharmacy")
    public MedicalRecord dispense(@PathVariable Long id) {
        MedicalRecord r = repo.findById(id).orElseThrow();
        r.status = "مكتمل (تم الصرف)";
        return repo.save(r);
    }
}
