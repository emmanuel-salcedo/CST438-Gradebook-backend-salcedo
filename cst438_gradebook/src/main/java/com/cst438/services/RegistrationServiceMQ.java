package com.cst438.services;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;

public class RegistrationServiceMQ extends RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public RegistrationServiceMQ() {
		System.out.println("MQ registration service ");
	}

	// ----- configuration of message queues

	@Autowired
	Queue registrationQueue;

	// ----- end of configuration of message queue

	// receiver of messages from Registration service

	@RabbitListener(queues = "gradebook-queue")
	@Transactional
	public void receive(EnrollmentDTO enrollmentDTO) {

		// check that this request is from the course instructor and for a valid course
		String email = "dwisneski@csumb.edu"; // user name (should be instructor's email)

		// get variables from enrollmentDTO
		int id = enrollmentDTO.id;
		String studentEmail = enrollmentDTO.studentEmail;
		String studentName = enrollmentDTO.studentEmail;
		int course_id = enrollmentDTO.course_id;

		// Verify that course and professor are valid
		Course course = courseRepository.findById(course_id).orElse(null);
		if (course == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course does not exist. ");
		}
		if (!course.getInstructor().equals(email)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Authorized. ");
		}

		// Create new enrollment from enrollmentDTO
		Enrollment newEnrollment = new Enrollment();
		newEnrollment.setCourse(course);
		newEnrollment.setId(id);
		newEnrollment.setStudentEmail(studentEmail);
		newEnrollment.setStudentName(studentName);

		// Save to database
		enrollmentRepository.save(newEnrollment);

	}

	// sender of messages to Registration Service
	@Override
	public void sendFinalGrades(int course_id, CourseDTOG courseDTO) {
		rabbitTemplate.convertAndSend(registrationQueue.getName(), courseDTO);
	
	}
}
