package com.motel.controller;

import com.motel.entity.Contract;
import com.motel.entity.Home;
import com.motel.entity.Room;
import com.motel.entity.Tenant;
import com.motel.entity.rentDetail;
import com.motel.service.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("room")
public class RoomController {
    @Autowired
    SessionFactory factory;
    @Autowired
    RoomService roomService;
    @Autowired
    HomeService homeService;
    @Autowired
    TenantService tenantService;
    @Autowired
    ContractService contractService;
    @Autowired
    RentDetailService rentDetailService;

    @Autowired
    HttpSession session;

    @RequestMapping(value = "index")
    public String index(ModelMap model, Integer offset, Integer maxResults, HttpServletRequest request) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        String homeId = request.getParameter("homeId");
        System.out.println(homeId);
        if (homeId == null || Objects.equals(homeId, "null")) {
            List<Room> room = roomService.getAllRoom(offset, maxResults);
            List<Home> home = homeService.getAllHome(offset, maxResults);
            model.addAttribute("home", home);
            model.addAttribute("room", room);
        } else {
            List<Room> room = roomService.getRoomByHomeId(homeId, offset, maxResults);
            List<Home> home = homeService.getAllHome(offset, maxResults);
            model.addAttribute("home", home);
            model.addAttribute("room", room);
        }
        return "room/index";
    }

    @RequestMapping(value = "insert", method = RequestMethod.GET)
    public String room_form(ModelMap model, Integer offset, Integer maxResults) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        List<Home> listHome = homeService.getAllHome(offset, maxResults);
        model.addAttribute("listHome", listHome);
        model.addAttribute("insertRoom", new Room());
        return "room/room_form";
    }


    @RequestMapping(value = "insert", method = RequestMethod.POST)
    public String room_form(ModelMap model, @ModelAttribute("insertRoom") Room room, Integer offset, Integer maxResults) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        List<Home> listHome = homeService.getAllHome(offset, maxResults);
        Home home = homeService.getHomeById(room.getHomeId());
        room.setHome(home);
        room.setCurrentQuantity(roomService.getCountFromContractId(room.getRoomId()));
        roomService.updateRoom(room);
        boolean check = roomService.insertRoom(room);
        if (check) {
            model.addAttribute("listHome", listHome);
            model.addAttribute("insertRoom", new Room());
            model.addAttribute("message", "Th??m th??nh c??ng");
        } else {
            model.addAttribute("listHome", listHome);
            model.addAttribute("insertRoom", new Room());
            model.addAttribute("fmessage", "Th??m th???t b???i");
        }
        return "room/room_form";
    }

    @RequestMapping(value = "showroom/{roomId}")
    public String showRoom(ModelMap model, @PathVariable("roomId") String roomId) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        Session session = factory.openSession();
        Contract contract = contractService.getActiveContractInARoom(roomId);
        int numOfMem = 0 ;
        Room room = (Room) session.get(Room.class, roomId);
        model.addAttribute("room", room);
   ;
        try {
            List<rentDetail> rentList = rentDetailService.getRentDetailFromContractId(contract.getContractId());
            numOfMem =rentList.size() ;
            model.addAttribute("currentQuantity",numOfMem );
            model.addAttribute("list", rentList);
        } catch (Exception e) {
        }
        model.addAttribute("currentQuantity",numOfMem );
        return "room/show_room";
    }

    @RequestMapping(value = "remove/{roomId}")
    public String removeRoom(ModelMap model, @PathVariable("roomId") String roomId, Integer offset, Integer maxResults) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        boolean check = roomService.removeRoom(roomId);
        if (check) {
            List<Room> listRoom = roomService.getAllRoom(offset, maxResults);
            model.addAttribute("room", listRoom);
            model.addAttribute("delete_message", "Xo?? ph??ng tr??? th??nh c??ng");
        } else {
            List<Room> listRoom = roomService.getAllRoom(offset, maxResults);
            model.addAttribute("room", listRoom);
            model.addAttribute("delete_message", "Xo?? ph??ng tr??? th???t b???i");
        }
        return "room/index";
    }

    @RequestMapping(value = "update/{roomId}", method = RequestMethod.GET)
    public String updateRoom(ModelMap model, @PathVariable("roomId") String roomId) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        Session session = factory.openSession();
        Room room = (Room) session.get(Room.class, roomId);
        model.addAttribute("room", room);
        System.out.println(room.getHomeId());
        model.addAttribute("homeId", room.getHomeId());
        return "room/room_update";
    }

    @RequestMapping(value = "update/{roomId}", method = RequestMethod.POST)
    public String updateRoom(ModelMap model, @ModelAttribute("updateRoom") Room room, String roomId, Integer offset, Integer maxResults) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:/login.htm";
        }
        Session session = factory.openSession();
        Home home = homeService.getHomeById(room.getHomeId());
        room.setHome(home);
        Contract contract = roomService.getContractFromRoomId(roomId);
        if (contract != null && room.getStatus() != 1) {
            model.addAttribute("update_message", "Ph??ng ??ang c?? ng?????i thu??, kh??ng th??? thay ?????i tr???ng th??i !");
            List<Room> listRoom = roomService.getAllRoom(offset, maxResults);
            model.addAttribute("room", room);
            return "room/show_room";
        }
        boolean check = roomService.updateRoom(room);
        if (check) {
            model.addAttribute("update_message", "C???p nh???t th??nh c??ng!");
            List<Room> listRoom = roomService.getAllRoom(offset, maxResults);
            model.addAttribute("room", room);
            return "room/show_room";
        } else {
            model.addAttribute("update_message", "C???p nh???t th???t b???i!");
            List<Room> listRoom = roomService.getAllRoom(offset, maxResults);
            model.addAttribute("room", room);
            return "room/show_room";
        }

    }

    @RequestMapping(value = "addTenant/{roomId}", method = RequestMethod.GET)
    public String addTenant(ModelMap model, @PathVariable("roomId") String roomId) {
        Session session = factory.openSession();
        Room room = (Room) session.get(Room.class, roomId);
        if (room.getStatus() == -1) {
            model.addAttribute("full_message", "Ph??ng hi???n ??ang s???a ch???a, vui l??ng c???p nh???t tr???ng th??i tr?????c khi th??m ");
            model.addAttribute("room", room);
            return "/room/show_room";
        }
        room.setCurrentQuantity(roomService.getCountFromContractId(room.getRoomId()));
        if (room.getCurrentQuantity() == room.getMax()) {
            model.addAttribute("full_message", "Ph??ng ???? ????? ng?????i, kh??ng th??? th??m ng?????i thu?? !");
            model.addAttribute("room", room);
            return "/room/show_room";
        }
        model.addAttribute("addTenant", new Tenant());
        model.addAttribute("roomId", roomId);
        model.addAttribute("price", room.getPrice());
        return "tenant/tenant_form";
    }

    @RequestMapping(value = "addTenant/{roomId}", method = RequestMethod.POST)
    public String addTenant(ModelMap model, Integer offset, Integer maxResults, HttpServletRequest request) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Contract newContract = new Contract();
        String roomId = request.getParameter("roomId");
        String CMND = request.getParameter("CMND");
        String name = request.getParameter("name");

        String gender = request.getParameter("gender");
        String numberPhone = request.getParameter("numberPhone");
        String address = request.getParameter("address");
        String date = request.getParameter("birthDay");
        String checkInDay = request.getParameter("checkIn");

        float price = Float.parseFloat(request.getParameter("price"));
        Date birthDay = java.sql.Date.valueOf(LocalDate.parse(date));
        Date checkIn = java.sql.Date.valueOf(LocalDate.parse(checkInDay));


        Tenant tenant = new Tenant(CMND, name, birthDay, gender, numberPhone, address, 1, null, null); //ng?????i thu??
        boolean check = tenantService.isInstanceTenant(CMND);
        if (check) {
            model.addAttribute("tenant_message", " Th??ng tin ng?????i thu?? n??y ???? c?? trong c?? s??? d??? li???u, m???i ho???t d???ng kh??c v???n ti???p t???c ???????c th???c hi???n");
        } else {
            boolean checkInsert = tenantService.insertTenant(tenant);
            if (!checkInsert) {
                model.addAttribute("fmessage", "Th??m th???t b???i");
                return "tenant/tenant_form";
            }
        }
        Contract activeContract = roomService.getActiveContractFromRoomId(roomId);
        if (activeContract == null) {
            Room room = (Room) session.get(Room.class, roomId);
            String contractId = roomId + '_' + checkInDay;
            Contract existContract = (Contract) session.get(Contract.class, contractId);

            if (existContract != null) {
                model.addAttribute("message", "M?? H???p ?????ng n??y ???? t???n t???i, h??y ki???m tra l???i th??ng tin ng??y b???t ?????u thu?? !!");
                return "tenant/tenant_form";
            } else {
                newContract.setContractId(contractId);
                newContract.setRoom(room);
                newContract.setRoomId(roomId);
                newContract.setCheckInDate(checkIn);
                newContract.setDepositMoney(price);
                newContract.setStatus(1);
                try {
                    session.save(newContract);
                } catch (Exception e) {
                    model.addAttribute("fmessage", " Th??m h???p ?????ng m???i b??? l???i, h??y th??? l???i");
                    return "tenant/tenant_form";
                }
            }

            rentDetail rent = new rentDetail();
            rent.setRentID(tenant.getCMND() + '_' + newContract.getContractId());
            rent.setTenant(tenant);
            rent.setContract(newContract);
            rent.setCheckInDate(checkIn);
            rent.setStatus(1);

            try {
                session.save(rent);
                t.commit();
                model.addAttribute("message", "Th??m th??nh c??ng");
            } catch (Exception e) {
                t.rollback();
                model.addAttribute("fessage", "Th??m th???t b???i");
            } finally {
                session.close();
            }
        } else {
            Contract contract = roomService.getActiveContractFromRoomId(roomId);
            rentDetail rent = new rentDetail();
            rent.setRentID(tenant.getCMND() + '_' + contract.getContractId());
            rent.setTenant(tenant);
            rent.setContract(contract);
            rent.setCheckInDate(checkIn);
            rent.setStatus(1);
            try {
                session.save(rent);
                t.commit();
                model.addAttribute("message", "Th??m th??nh c??ng");
            } catch (Exception e) {
                t.rollback();
                model.addAttribute("fmessage", "Th??m th???t b???i");
            } finally {
                session.close();
            }
        }

        return "tenant/tenant_form";
    }

    ;
}


