import request from "@/utils/request.js";

//房间/床位管理
const roomApi = {
    //房间
    list(query) {
        return request.get("/rooms", {params: query});
    },
    add(data) {
        return request.post("/rooms", data);
    },
    update(id, data) {
        return request.put(`/rooms/${id}`, data);
    },
    deleteById(id) {
        return request.delete(`/rooms/${id}`);
    },
    //床位
    bedsByRoom(roomId) {
        return request.get(`/beds/room/${roomId}`);
    },
    freeBeds() {
        return request.get("/beds/free");
    },
    addBeds(roomId, bedNos) {
        return request.post("/beds", {roomId, bedNos});
    },
    updateBedStatus(id, status) {
        return request.put(`/beds/${id}/status`, {status});
    },
    deleteBed(id) {
        return request.delete(`/beds/${id}`);
    }
}

export default roomApi
