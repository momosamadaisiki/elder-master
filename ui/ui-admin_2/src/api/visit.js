import request from "@/utils/request.js";

//探视/访客登记
const visitApi = {
    list(query) {
        return request.get("/visits", {params: query});
    },
    //applyWay: 0线上申请 1现场登记
    add(data) {
        return request.post("/visits", data);
    },
    approve(id) {
        return request.put(`/visits/${id}/approve`);
    },
    reject(id, remark) {
        return request.put(`/visits/${id}/reject`, null, {params: {remark}});
    },
    arrive(id) {
        return request.put(`/visits/${id}/arrive`);
    },
    leave(id) {
        return request.put(`/visits/${id}/leave`);
    },
    expire(id) {
        return request.put(`/visits/${id}/expire`);
    },
    deleteById(id) {
        return request.delete(`/visits/${id}`);
    }
}

export default visitApi
