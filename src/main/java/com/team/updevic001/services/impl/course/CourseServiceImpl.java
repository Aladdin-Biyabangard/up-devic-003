package com.team.updevic001.services.impl.course;

import com.team.updevic001.dao.entities.auth.User;
import com.team.updevic001.dao.entities.course.Course;
import com.team.updevic001.dao.entities.course.CourseRating;
import com.team.updevic001.dao.entities.course.WishList;
import com.team.updevic001.dao.repositories.CourseRatingRepository;
import com.team.updevic001.dao.repositories.CourseRepository;
import com.team.updevic001.dao.repositories.LessonRepository;
import com.team.updevic001.dao.repositories.UserCourseFeeRepository;
import com.team.updevic001.dao.repositories.WishListRepository;
import com.team.updevic001.exceptions.ForbiddenException;
import com.team.updevic001.exceptions.NotFoundException;
import com.team.updevic001.model.dtos.page.CustomPage;
import com.team.updevic001.model.dtos.page.CustomPageRequest;
import com.team.updevic001.model.dtos.request.CourseDto;
import com.team.updevic001.model.dtos.response.course.ResponseCategoryDto;
import com.team.updevic001.model.dtos.response.course.ResponseCourseDto;
import com.team.updevic001.model.dtos.response.course.ResponseCourseShortInfoDto;
import com.team.updevic001.model.dtos.response.course.ResponseFullCourseDto;
import com.team.updevic001.model.enums.CourseCategoryType;
import com.team.updevic001.model.mappers.CourseMapper;
import com.team.updevic001.services.impl.notification.NotificationService;
import com.team.updevic001.services.impl.common.DeleteService;
import com.team.updevic001.services.interfaces.CourseService;
import com.team.updevic001.services.interfaces.FileLoadService;
import com.team.updevic001.services.interfaces.TeacherService;
import com.team.updevic001.specification.CourseSpecification;
import com.team.updevic001.specification.criteria.CourseSearchCriteria;
import com.team.updevic001.utility.AuthHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.team.updevic001.exceptions.ExceptionConstants.COURSE_NOT_FOUND;
import static com.team.updevic001.exceptions.ExceptionConstants.FORBIDDEN_EXCEPTION;
import static com.team.updevic001.utility.IDGenerator.normalizeString;
import static com.team.updevic001.utility.PercentageCalculation.calculatePercentage;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {


    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final ModelMapper modelMapper;
    private final FileLoadService fileLoadServiceImpl;
    private final CourseRatingRepository courseRatingRepository;
    private final AuthHelper authHelper;
    private final WishListRepository wishListRepository;
    private final LessonRepository lessonRepository;
    private final DeleteService deleteService;
    private final UserCourseFeeRepository userCourseFeeRepository;
    private final TeacherService teacherService;
    private final NotificationService notificationService;
    private final BigDecimal percentage = BigDecimal.valueOf(5);
    private static final String COURSE_LINK = "https://up-devic-001.lovable.app/courses/";

    @Override
    @Transactional
    @CacheEvict(value = {"courseSearchCache", "courseSortCache"}, allEntries = true)
    public ResponseCourseDto createCourse(CourseCategoryType courseCategoryType,
                                          CourseDto courseDto) {
        var teacher = authHelper.getAuthenticatedUser();

        var course = modelMapper.map(courseDto, Course.class);
        course.setPrice(courseDto.getPrice() + calculatePercentage(BigDecimal.valueOf(courseDto.getPrice()), percentage).doubleValue());
        course.setPriceWithoutInterest(BigDecimal.valueOf(courseDto.getPrice()));
        String courseId = normalizeString(course.getTitle());
        course.setId(courseId);
        course.setCourseCategoryType(courseCategoryType);
        course.setTeacher(teacher);
        courseRepository.save(course);
        notificationService.sendNotificationForCreationNewCourse(courseCategoryType, course.getTitle(), COURSE_LINK + courseId);
        return courseMapper.courseDto(course);
    }

    @Override
    @Transactional
    public void updateCourse(String courseId, CourseDto courseDto) {
        var teacher = authHelper.getAuthenticatedUser();

        validateAccess(courseId, teacher);

        var findCourse = courseRepository
                .findById(courseId).orElseThrow(() -> new NotFoundException(COURSE_NOT_FOUND.getCode(),
                        COURSE_NOT_FOUND.getMessage().formatted(courseId)));

        modelMapper.map(courseDto, findCourse);
        findCourse.setPrice(courseDto.getPrice() + calculatePercentage(BigDecimal.valueOf(courseDto.getPrice()), percentage).doubleValue());
        findCourse.setPriceWithoutInterest(BigDecimal.valueOf(courseDto.getPrice()));
        courseRepository.save(findCourse);
    }

    public void uploadCoursePhoto(String courseId, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Multipart file is empty or null!");
        }
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException(COURSE_NOT_FOUND.getCode(),
                    COURSE_NOT_FOUND.getMessage().formatted(courseId));
        }
        String photoOfWhat = "coursePhoto";
        var fileUploadResponse = fileLoadServiceImpl.uploadFile(multipartFile, courseId, photoOfWhat);
        courseRepository.updateCourseFileInfo(courseId, fileUploadResponse.getKey(), fileUploadResponse.getUrl());
    }

    @Override
    @Transactional
    public void updateRatingCourse(String courseId, int rating) {
        User user = authHelper.getAuthenticatedUser();
        Course course = findCourseById(courseId);

        CourseRating courseRating = courseRatingRepository
                .findCourseRatingByCourseAndUser(course, user)
                .orElseGet(() -> CourseRating.builder()
                        .course(course)
                        .user(user)
                        .build());

        courseRating.setRating(rating);
        courseRatingRepository.save(courseRating);
        course.setRating(getAverageRating(course));
    }

    @Transactional
    @Override
    public ResponseFullCourseDto getCourse(String courseId) {
        var course = courseRepository.findCourseById(courseId)
                .orElseThrow(() -> new NotFoundException(COURSE_NOT_FOUND.getCode(),
                        COURSE_NOT_FOUND.getMessage().formatted(courseId)));

        boolean paid = false;
        try {
            var user = authHelper.getAuthenticatedUser();

            if (userCourseFeeRepository.existsUserCourseFeeByCourseAndUser(course, user)) {
                paid = true;
            } else if (course.getTeacher().equals(user)) {
                paid = true;
            }
        } catch (Exception ex) {
            paid = false;
        }

        var teacherShortInfo = teacherService.getTeacherShortInfo(course.getTeacher());

        // Burada artıq collections açıqdır
        var courseResponse = courseMapper.toFullResponse(course, teacherShortInfo);

        // Collections-i yeni Set/List ilə kopyala ki, Hibernate proxy yox olsun
        courseResponse.setSearchKeys(new HashSet<>(course.getSearchKeys()));
        courseResponse.setTags(new HashSet<>(course.getTags()));
        courseResponse.setPaid(paid);

        return courseResponse;
    }


    @Override
    public CustomPage<ResponseCourseShortInfoDto> search(CourseSearchCriteria searchCriteria,
                                                         CustomPageRequest request) {

        int page = (request != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request != null && request.getSize() > 0) ? request.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size);

        boolean hasFilters = searchCriteria != null && (
                searchCriteria.getName() != null ||
                        searchCriteria.getLevel() != null ||
                        searchCriteria.getMinPrice() != null ||
                        searchCriteria.getMaxPrice() != null ||
                        searchCriteria.getCourseCategoryType() != null
        );

        Page<Course> resultPage;

        if (hasFilters) {
            resultPage = courseRepository.findAll(
                    CourseSpecification.buildSpecification(searchCriteria),
                    pageable
            );
        } else {
            resultPage = courseRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return new CustomPage<>(
                courseMapper.toCourseResponse(resultPage.getContent()),
                resultPage.getNumber(),
                resultPage.getSize()
        );
    }

    @Override
    @Cacheable(value = "categories")
    public List<ResponseCategoryDto> getCategories() {
        return Arrays.stream(CourseCategoryType.values())
                .map(type -> {
                    long courCount = courseRepository.countByCourseCategoryType(type);
                    return new ResponseCategoryDto(type, courCount);
                })
                .toList();
    }

    @Override
    public CustomPage<ResponseCourseShortInfoDto> getWishList(CustomPageRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        var authenticatedUser = authHelper.getAuthenticatedUser();
        var courseByUser = wishListRepository.findCourseByUser(authenticatedUser, pageable);
        return new CustomPage<>(
                courseMapper.toCourseResponse(courseByUser.getContent()),
                courseByUser.getNumber(),
                courseByUser.getSize()
        );
    }

    @Override
    public List<ResponseCourseShortInfoDto> getMost5PopularCourses() {
        var top5ByOrderByRatingDesc = courseRepository.findTop5ByOrderByRatingDesc();
        return courseMapper.toCourseResponse(top5ByOrderByRatingDesc);
    }


    @Override
    public void wishListFunction(String courseId) {
        var authenticatedUser = authHelper.getAuthenticatedUser();
        var course = findCourseById(courseId);
        if (wishListRepository.existsWishListByCourseAndUser(course, authenticatedUser)) {
            wishListRepository.deleteWishListByCourseIdAndUser(courseId, authenticatedUser);
        }
        var wishList = WishList.builder()
                .course(course)
                .user(authenticatedUser)
                .build();
        wishListRepository.save(wishList);
    }

    @Override
    @CacheEvict(value = {"courseSearchCache", "courseSortCache"}, allEntries = true)
    @Transactional
    public void deleteCourse(String courseId) {
        var teacher = authHelper.getAuthenticatedUser();
        validateAccess(courseId, teacher);
        var allLessonIdsByCourseId = lessonRepository.findAllLessonIdsByCourseId(courseId);
        deleteService.deleteCourseAndReferencedData(courseId, allLessonIdsByCourseId, teacher);
    }

    public Course findCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(COURSE_NOT_FOUND.getCode(),
                        COURSE_NOT_FOUND.getMessage().formatted(courseId)));
    }

    private double getAverageRating(Course course) {
        List<CourseRating> ratings = courseRatingRepository.findByCourse(course);
        return ratings.stream()
                .mapToInt(CourseRating::getRating)
                .average()
                .orElse(0.0);
    }

    protected void validateAccess(String courseId, User teacher) {
        if (!courseRepository.existsCourseByIdAndTeacher(courseId, teacher)) {
            throw new ForbiddenException(FORBIDDEN_EXCEPTION.getCode(), "User not allowed!");
        }
    }

}
