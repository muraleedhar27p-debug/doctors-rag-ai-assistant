package com.spring.ai.rag.config;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;

    @Autowired
    private final VectorStore vectorStore;

    @Override
    public void run(String... args) {
        log.info("Seeding H2 database with doctor records...");

        List<Doctor> doctors = List.of(

                Doctor.builder()
                        .name("Dr. Anjali Mehta")
                        .department("Cardiology")
                        .role("Senior Cardiologist")
                        .email("anjali.mehta@hospital.com")
                        .phone("+1-555-1001")
                        .yearsExperience(14)
                        .skills("Echocardiography, Cardiac Catheterization, Heart Failure Management, ECG Interpretation, Angioplasty")
                        .salaryBand("Attending-L3")
                        .location("New York, NY")
                        .managerName("Dr. Robert Hayes")
                        .bio("Dr. Mehta specializes in interventional cardiology with a focus on heart failure and coronary artery disease. She has performed over 2,000 catheterization procedures and leads the hospital's cardiac rehabilitation program. Published researcher in the Journal of the American College of Cardiology.").build(),

                Doctor.builder()
                        .name("Dr. Robert Hayes")
                        .department("Cardiology")
                        .role("Chief of Cardiology")
                        .email("robert.hayes@hospital.com")
                        .phone("+1-555-1002")
                        .yearsExperience(24)
                        .skills("Electrophysiology, Ablation Therapy, Pacemaker Implantation, Clinical Leadership, Research")
                        .salaryBand("Chief")
                        .location("New York, NY")
                        .managerName("Medical Director")
                        .bio("Dr. Hayes leads the cardiology department of 18 physicians. He is a pioneer in cardiac electrophysiology and has trained over 50 fellows. Board-certified in Cardiovascular Disease and Clinical Cardiac Electrophysiology. Recipient of the American Heart Association Lifetime Achievement Award.").build(),

                Doctor.builder()
                        .name("Dr. Priya Nair")
                        .department("Neurology")
                        .role("Neurologist")
                        .email("priya.nair@hospital.com")
                        .phone("+1-555-2001")
                        .yearsExperience(9)
                        .skills("EEG Interpretation, Stroke Management, Epilepsy Treatment, Multiple Sclerosis, Lumbar Puncture")
                        .salaryBand("Attending-L2")
                        .location("Boston, MA")
                        .managerName("Dr. Samuel Torres")
                        .bio("Dr. Nair focuses on stroke neurology and epilepsy management. She established the hospital's rapid stroke response protocol, reducing door-to-needle time by 40%. Actively involved in clinical trials for novel epilepsy medications. Member of the American Academy of Neurology.").build(),

                Doctor.builder()
                        .name("Dr. Samuel Torres")
                        .department("Neurology")
                        .role("Chief of Neurology")
                        .email("samuel.torres@hospital.com")
                        .phone("+1-555-2002")
                        .yearsExperience(20)
                        .skills("Neurocritical Care, Movement Disorders, Deep Brain Stimulation, Parkinson's Disease, Research Leadership")
                        .salaryBand("Chief")
                        .location("Boston, MA")
                        .managerName("Medical Director")
                        .bio("Dr. Torres is a nationally recognized expert in movement disorders and Parkinson's disease. He leads a team of 12 neurologists and has published over 80 peer-reviewed articles. He directs the hospital's Deep Brain Stimulation program, one of the largest in New England.").build(),

                Doctor.builder()
                        .name("Dr. Fatima Al-Rashid")
                        .department("Oncology")
                        .role("Medical Oncologist")
                        .email("fatima.alrashid@hospital.com")
                        .phone("+1-555-3001")
                        .yearsExperience(11)
                        .skills("Chemotherapy, Immunotherapy, Breast Cancer, Lung Cancer, Clinical Trials")
                        .salaryBand("Attending-L2")
                        .location("Houston, TX")
                        .managerName("Dr. Michael Grant")
                        .bio("Dr. Al-Rashid specializes in breast and lung cancer treatment with a focus on precision oncology. She manages a panel of 120+ cancer patients and is the principal investigator on two active clinical trials. Fluent in Arabic, English, and French, enabling care for diverse patient populations.").build(),

                Doctor.builder()
                        .name("Dr. Michael Grant")
                        .department("Oncology")
                        .role("Director of Oncology")
                        .email("michael.grant@hospital.com")
                        .phone("+1-555-3002")
                        .yearsExperience(22)
                        .skills("Hematologic Malignancies, Bone Marrow Transplant, CAR-T Cell Therapy, Tumor Board Leadership, Grant Writing")
                        .salaryBand("Director")
                        .location("Houston, TX")
                        .managerName("Medical Director")
                        .bio("Dr. Grant directs one of the top-ranked oncology programs in the South. He pioneered the hospital's CAR-T cell therapy program and has secured over $8M in NIH research grants. Author of three oncology textbook chapters and over 100 peer-reviewed publications.").build(),

                Doctor.builder()
                        .name("Dr. James Osei")
                        .department("Emergency Medicine")
                        .role("Emergency Physician")
                        .email("james.osei@hospital.com")
                        .phone("+1-555-4001")
                        .yearsExperience(8)
                        .skills("Trauma Resuscitation, Airway Management, POCUS, Toxicology, Critical Care")
                        .salaryBand("Attending-L2")
                        .location("Chicago, IL")
                        .managerName("Dr. Laura Kim")
                        .bio("Dr. Osei manages high-acuity cases in a Level I trauma center seeing 80,000+ visits per year. He is an expert in point-of-care ultrasound (POCUS) and serves as the department's POCUS training coordinator. Certified in Advanced Trauma Life Support (ATLS) and Pediatric Advanced Life Support (PALS).").build(),

                Doctor.builder()
                        .name("Dr. Laura Kim")
                        .department("Emergency Medicine")
                        .role("Chair of Emergency Medicine")
                        .email("laura.kim@hospital.com")
                        .phone("+1-555-4002")
                        .yearsExperience(18)
                        .skills("Mass Casualty Incident Management, Quality Improvement, ED Operations, Simulation Training, Sepsis Protocols")
                        .salaryBand("Chair")
                        .location("Chicago, IL")
                        .managerName("Medical Director")
                        .bio("Dr. Kim leads a department of 35 emergency physicians and 60 nurses. She redesigned the ED triage workflow, reducing average door-to-physician time from 28 to 11 minutes. Regional coordinator for disaster preparedness and mass casualty response.").build(),

                Doctor.builder()
                        .name("Dr. Elena Volkov")
                        .department("Pediatrics")
                        .role("Pediatric Hospitalist")
                        .email("elena.volkov@hospital.com")
                        .phone("+1-555-5001")
                        .yearsExperience(7)
                        .skills("Inpatient Pediatrics, Neonatal Care, Asthma Management, Fever Evaluation, Family Communication")
                        .salaryBand("Attending-L2")
                        .location("Seattle, WA")
                        .managerName("Dr. David Okonkwo")
                        .bio("Dr. Volkov cares for hospitalized children from newborns to adolescents. She is passionate about family-centered care and leads the department's communication skills training for residents. Fluent in Russian, English, and basic Spanish. Recognized for outstanding patient satisfaction scores three years running.").build(),

                Doctor.builder()
                        .name("Dr. David Okonkwo")
                        .department("Pediatrics")
                        .role("Chief of Pediatrics")
                        .email("david.okonkwo@hospital.com")
                        .phone("+1-555-5002")
                        .yearsExperience(21)
                        .skills("Pediatric Critical Care, PICU Management, Septic Shock, Ventilator Management, Medical Education")
                        .salaryBand("Chief")
                        .location("Seattle, WA")
                        .managerName("Medical Director")
                        .bio("Dr. Okonkwo leads a 200-bed pediatric service and a 24-bed PICU. He is a fellow of the American Academy of Pediatrics and the Society of Critical Care Medicine. He has mentored over 60 pediatric residents and is widely recognized for excellence in medical education.").build(),

                Doctor.builder()
                        .name("Dr. Sophia Nguyen")
                        .department("Orthopedic Surgery")
                        .role("Orthopedic Surgeon")
                        .email("sophia.nguyen@hospital.com")
                        .phone("+1-555-6001")
                        .yearsExperience(10)
                        .skills("Joint Replacement, Arthroscopy, Sports Medicine, Fracture Repair, Robotic Surgery")
                        .salaryBand("Attending-L2")
                        .location("Los Angeles, CA")
                        .managerName("Dr. Thomas Reyes")
                        .bio("Dr. Nguyen specializes in minimally invasive hip and knee replacement using robotic assistance. She has performed over 1,500 joint replacement surgeries with a complication rate well below the national average. Official team physician for a professional basketball franchise.").build(),

                Doctor.builder()
                        .name("Dr. Thomas Reyes")
                        .department("Orthopedic Surgery")
                        .role("Chief of Orthopedic Surgery")
                        .email("thomas.reyes@hospital.com")
                        .phone("+1-555-6002")
                        .yearsExperience(26)
                        .skills("Spine Surgery, Spinal Fusion, Scoliosis Correction, Complex Reconstruction, Surgical Training")
                        .salaryBand("Chief")
                        .location("Los Angeles, CA")
                        .managerName("Medical Director")
                        .bio("Dr. Reyes is a world-renowned spine surgeon who has performed over 4,000 spinal surgeries. He leads a department of 14 orthopedic surgeons and is a professor of surgery at the affiliated medical school. His research on minimally invasive spine techniques has been cited over 500 times.").build(),

                Doctor.builder()
                        .name("Dr. Amir Hassan")
                        .department("Psychiatry")
                        .role("Psychiatrist")
                        .email("amir.hassan@hospital.com")
                        .phone("+1-555-7001")
                        .yearsExperience(12)
                        .skills("Psychopharmacology, CBT, Inpatient Psychiatry, Mood Disorders, Addiction Medicine")
                        .salaryBand("Attending-L2")
                        .location("San Francisco, CA")
                        .managerName("Dr. Claire Dubois")
                        .bio("Dr. Hassan specializes in treatment-resistant depression and bipolar disorder. He oversees the hospital's inpatient psychiatric unit of 30 beds and is certified in Transcranial Magnetic Stimulation (TMS) therapy. Active advocate for mental health destigmatization in South Asian communities.").build(),

                Doctor.builder()
                        .name("Dr. Claire Dubois")
                        .department("Psychiatry")
                        .role("Chair of Psychiatry")
                        .email("claire.dubois@hospital.com")
                        .phone("+1-555-7002")
                        .yearsExperience(19)
                        .skills("Forensic Psychiatry, Child and Adolescent Psychiatry, PTSD, Trauma-Informed Care, Policy Advocacy")
                        .salaryBand("Chair")
                        .location("San Francisco, CA")
                        .managerName("Medical Director")
                        .bio("Dr. Dubois leads one of the most comprehensive psychiatry programs on the West Coast. She is a consultant to the court system on forensic psychiatric evaluations and has testified as an expert witness over 50 times. Recipient of the APA Distinguished Service Award.").build(),

                Doctor.builder()
                        .name("Dr. Kwame Mensah")
                        .department("Internal Medicine")
                        .role("Hospitalist")
                        .email("kwame.mensah@hospital.com")
                        .phone("+1-555-8001")
                        .yearsExperience(6)
                        .skills("Inpatient Medicine, Diabetes Management, COPD, Sepsis, Care Transitions")
                        .salaryBand("Attending-L1")
                        .location("Atlanta, GA")
                        .managerName("Dr. Sarah Patel")
                        .bio("Dr. Mensah manages complex inpatient medical cases on a busy 40-bed medical floor. He championed the hospital's care transitions program, reducing 30-day readmission rates by 22%. Passionate about health equity and serves on the hospital's DEI medical committee.").build(),

                Doctor.builder()
                        .name("Dr. Sarah Patel")
                        .department("Internal Medicine")
                        .role("Chief of Hospital Medicine")
                        .email("sarah.patel@hospital.com")
                        .phone("+1-555-8002")
                        .yearsExperience(17)
                        .skills("Quality Improvement, Patient Safety, Value-Based Care, Hospital Operations, Clinical Informatics")
                        .salaryBand("Chief")
                        .location("Atlanta, GA")
                        .managerName("Medical Director")
                        .bio("Dr. Patel leads a hospitalist group of 22 physicians. She implemented a clinical decision support system that reduced medication errors by 35%. Named one of Modern Healthcare's Top 25 Emerging Leaders. Author of a widely-used textbook on hospital medicine.").build(),

                Doctor.builder()
                        .name("Dr. Ingrid Larsson")
                        .department("Radiology")
                        .role("Diagnostic Radiologist")
                        .email("ingrid.larsson@hospital.com")
                        .phone("+1-555-9001")
                        .yearsExperience(13)
                        .skills("MRI Interpretation, CT Scan, Mammography, AI-Assisted Diagnosis, Breast Imaging")
                        .salaryBand("Attending-L3")
                        .location("Minneapolis, MN")
                        .managerName("Dr. Carlos Vega")
                        .bio("Dr. Larsson is a subspecialty radiologist in breast imaging, reading over 5,000 mammograms annually. She led the implementation of AI-assisted mammography screening, improving early detection rates by 18%. Certified in ACR Breast Imaging Reporting and Data System (BI-RADS).").build(),

                Doctor.builder()
                        .name("Dr. Carlos Vega")
                        .department("Radiology")
                        .role("Chief of Radiology")
                        .email("carlos.vega@hospital.com")
                        .phone("+1-555-9002")
                        .yearsExperience(23)
                        .skills("Interventional Radiology, IR Procedures, Vascular Imaging, Radiomics, Department Leadership")
                        .salaryBand("Chief")
                        .location("Minneapolis, MN")
                        .managerName("Medical Director")
                        .bio("Dr. Vega leads a radiology department processing over 200,000 imaging studies per year. He is a national expert in interventional radiology and pioneered minimally invasive tumor embolization techniques at the institution. President-elect of the Radiological Society of North America.").build(),

                Doctor.builder()
                        .name("Dr. Yuki Shimizu")
                        .department("Anesthesiology")
                        .role("Anesthesiologist")
                        .email("yuki.shimizu@hospital.com")
                        .phone("+1-555-0201")
                        .yearsExperience(9)
                        .skills("General Anesthesia, Regional Anesthesia, Pain Management, Critical Care, Pediatric Anesthesia")
                        .salaryBand("Attending-L2")
                        .location("Denver, CO")
                        .managerName("Dr. Marcus Webb")
                        .bio("Dr. Shimizu provides anesthesia for complex cardiac, thoracic, and pediatric surgeries. She is the department's lead for regional anesthesia and chronic pain procedures. Developed a multimodal analgesia protocol that reduced opioid consumption by 30% post-surgery.").build(),

                Doctor.builder()
                        .name("Dr. Marcus Webb")
                        .department("Anesthesiology")
                        .role("Chief of Anesthesiology")
                        .email("marcus.webb@hospital.com")
                        .phone("+1-555-0202")
                        .yearsExperience(25)
                        .skills("Cardiac Anesthesia, Neuroanesthesia, Perioperative Medicine, Simulation, Patient Safety")
                        .salaryBand("Chief")
                        .location("Denver, CO")
                        .managerName("Medical Director")
                        .bio("Dr. Webb oversees anesthesia services for over 15,000 surgical cases per year. He is a certified patient safety officer and has chaired the hospital's operating room safety committee for 10 years. His simulation-based training curriculum has been adopted by 12 residency programs nationally.").build()
        );

        doctorRepository.saveAll(doctors);
        log.info("Seeded {} doctor records into H2 database.", doctors.size());
        indexDoctors();
    }

    public void indexDoctors() {
        log.info("Starting doctor indexing into vector store...");
        List<Doctor> doctors = doctorRepository.findAll();

        List<Document> documents = doctors.stream()
                .map(doc -> new Document(
                        doc.toDocument(),
                        Map.of(
                                "doctorId", doc.getId().toString(),
                                "name",       doc.getName(),
                                "department", doc.getDepartment(),
                                "role",       doc.getRole()
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("Indexed {} doctor documents into vector store.", documents.size());
    }
}