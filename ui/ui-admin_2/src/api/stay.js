import request from "@/utils/request.js";

//入住/退住/结算
const stayApi = {
    list(query) {
        return request.get("/stays", {params: query});
    },
    candidates() {
        return request.get("/stays/candidates");
    },
    checkIn(data) {
        return request.post("/stays/check-in", data);
    },
    transfer(id, bedId) {
        return request.put(`/stays/${id}/transfer`, {bedId});
    },
    checkOut(id, remark) {
        return request.put(`/stays/${id}/check-out`, {remark});
    },
    pay(id) {
        return request.put(`/stays/${id}/pay`);
    }
}

export default stayApi
