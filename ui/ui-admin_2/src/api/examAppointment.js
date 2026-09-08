import request from "@/utils/request.js";

//后台：体检登记/完成
const examAppointmentApi = {
    list(query) {
        return request.get("/exam-appointments", {params: query});
    },
    detail(id) {
        return request.get(`/exam-appointments/${id}`);
    },
    saveItems(id, items) {
        return request.put(`/exam-appointments/${id}/items`, {items});
    },
    cancel(id) {
        return request.put(`/exam-appointments/${id}/cancel`);
    }
}

export default examAppointmentApi
