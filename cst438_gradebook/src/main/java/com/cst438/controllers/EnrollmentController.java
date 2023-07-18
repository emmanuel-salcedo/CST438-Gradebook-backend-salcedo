package com.cst438.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;

@RestController
public class EnrollmentController {

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	EnrollmentRepository enrollmentRepository;

	/*
	 * endpoint used by registration service to add an enrollment to an existing
	 * course.
	 */
	@PostMapping("/enrollment")
	@Transactional
	public EnrollmentDTO addEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {

		// check that this request is from the course instructor and for a valid course
		String email = "dwisneski@csumb.edu"; // user name (should be instructor's email)

		// Grab variables from enrollmentDTO
		int id = enrollmentDTO.id;
		String studentEmail = enrollmentDTO.studentEmail;
		String studentName = enrollmentDTO.studentEmail;
		int course_id = enrollmentDTO.course_id;

		// verify that course id and professor are valid
		Course course = courseRepository.findById(course_id).orElse(null);
		if (course == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course does not exist. ");
		}
		if (!course.getInstructor().equals(email)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Authorized. ");
		}

		// Create new enrollment based on enrollmentDTO
		Enrollment newEnrollment = new Enrollment();
		newEnrollment.setCourse(course);
		newEnrollment.setId(id);
		newEnrollment.setStudentEmail(studentEmail);
		newEnrollment.setStudentName(studentName);

		// Save enrollment to database
		enrollmentRepository.save(newEnrollment);

		return null;

	}

}
