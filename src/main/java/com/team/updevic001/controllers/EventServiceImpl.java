//package com.team.updevic001.controllers;
//
//import com.updevic.entity.Event;
//import com.updevic.repository.EventRepository;
//import com.updevic.service.EventService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class EventServiceImpl implements EventService {
//
//    private final EventRepository repository;
//
//    @Override
//    public Event createEvent(Event event) {
//        event.setId(null); // ID backend tərəfindən yaradılacaq
//        return repository.save(event);
//    }
//
//    @Override
//    public Event updateEvent(String id, Event event) {
//        Event existing = getEventById(id);
//        event.setId(existing.getId());
//        return repository.save(event);
//    }
//
//    @Override
//    public void deleteEvent(String id) {
//        repository.deleteById(id);
//    }
//
//    @Override
//    public Event getEventById(String id) {
//        return repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Event not found"));
//    }
//
//    @Override
//    public Page<Event> searchEvents(String title, String category, String dateFrom, String dateTo, Pageable pageable) {
//        Specification<Event> spec = Specification.where(null);
//
//        if (title != null && !title.isBlank())
//            spec = spec.and((root, query, cb) ->
//                    cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
//
//        if (category != null && !category.isBlank())
//            spec = spec.and((root, query, cb) ->
//                    cb.equal(root.get("category"), category));
//
//        if (dateFrom != null && !dateFrom.isBlank())
//            spec = spec.and((root, query, cb) ->
//                    cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
//
//        if (dateTo != null && !dateTo.isBlank())
//            spec = spec.and((root, query, cb) ->
//                    cb.lessThanOrEqualTo(root.get("date"), dateTo));
//
//        return repository.findAll(spec, pageable);
//    }
//}
