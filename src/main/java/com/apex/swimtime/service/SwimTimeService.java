package com.apex.swimtime.service;

import com.apex.swimtime.constants.Split;
import com.apex.swimtime.constants.StrokeEnum;
import com.apex.swimtime.constants.SwimTime;
import com.apex.swimtime.constants.SwimTimeRO;
import com.apex.swimtime.repository.SplitRepository;
import com.apex.swimtime.repository.SwimTimeRepository;
import com.apex.swimtime.repository.SwimmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SwimTimeService {

    @Autowired
    private SwimTimeRepository swimTimeRepository;

    @Autowired
    private SplitRepository splitRepository;

    @Autowired
    private SwimmerRepository swimmerRepository;

    public List<SwimTimeRO> getTimesByEvent(StrokeEnum stroke, Integer distance) {
        List<Integer> timeIDs = swimTimeRepository.findByEvent(stroke.ordinal(), distance);

        return getTimes(timeIDs);
    }

    public List<SwimTimeRO> getTimesBySwimmerID(Integer swimmerID) {
        List<Integer> timeIDs = swimTimeRepository.findBySwimmerID(swimmerID);

        return getTimes(timeIDs);
    }

    private List<SwimTimeRO> getTimes(List<Integer> timeIDs) {
        List<SwimTimeRO> times = new ArrayList<>();

        for(Integer timeID : timeIDs) {
            SwimTimeRO time = new SwimTimeRO();
            SwimTime swimTime = swimTimeRepository.findById(timeID).get();
            time.setSwimmerID(swimTime.getSwimmerID());
            time.setDate(swimTime.getDate());
            time.setTime(swimTime.getTime());
            time.setStroke(swimTime.getStroke());
            time.setDistance(swimTime.getDistance());
            time.setTimeID(swimTime.getTimeID());
            time.setSplits(splitRepository.findById(swimTime.getTimeID()).get());
            times.add(time);
        }

        return times;
    }

    //TODO:getSplitsByTimeID
    //TODO:getTimesByStroke
    //TODO:getTimesByDistance
    //TODO:getTimesByDate
    //TODO:getTimesByDateRange


    public List<SwimTimeRO> addTimes(List<SwimTimeRO> times) {
        for(SwimTimeRO time :times) {
            addSwimTime(time);
        }

        return times;
    }

    public SwimTime addSwimTime(SwimTimeRO time){
        //TODO: Better Data validation
        SwimTime entryTime = new SwimTime();

        if(time.getSwimmerID()!= null && time.getSwimmerID() >= 0) {
            entryTime.setSwimmerID(time.getSwimmerID());
        }

        if(time.getDate()!= null ) {
            entryTime.setDate(time.getDate());
        }

        if(time.getDistance() > 0) {
            entryTime.setDistance(time.getDistance());
        }

        if(time.getStroke() != null) {
            entryTime.setStroke(time.getStroke());

        }

        if(time.getTime() != null && time.getTime() >= 0) {
            entryTime.setTime(time.getTime());
        }

        swimTimeRepository.save(entryTime);

        if(time.getSplits() != null &&
                (time.getSplits().getSplit1() != null && time.getSplits().getSplit1() > 0))
        {
            Split entrySplits = time.getSplits();
            entrySplits.setTimeID(entryTime.getTimeID());
            splitRepository.save(entrySplits);
        }

        return entryTime;
    }

    public Integer deleteTime(Integer timeID) {
        if(splitRepository.findById(timeID).isPresent()) {
            splitRepository.deleteById(timeID);
        }

        swimTimeRepository.deleteById(timeID);

        return timeID;
    }

    public Integer deleteTimes(Integer swimmerID) {
        //TODO: Data validation for deletes
        List<Integer> timeIDs = swimTimeRepository.findBySwimmerID(swimmerID);

        for(Integer timeID : timeIDs) {
            deleteTime(timeID);
        }
        return swimmerID;
    }

    public Integer deleteSwimmer(Integer swimmerID) {
        if(swimmerRepository.findById(swimmerID).isPresent()) {
            deleteTimes(swimmerID);
            swimmerRepository.deleteById(swimmerID);
        }

        return swimmerID;
    }
}
