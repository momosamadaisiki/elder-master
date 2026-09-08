import request from "@/utils/request.js";

//老人信息扩展：家属 / 收费项目 / 月度账单
const elderProfileApi = {
    //家属
    familyList(elderId) {
        return request.get("/elder-families", {params: {elderId}});
    },
    familyAdd(data) {
        return request.post("/elder-families", data);
    },
    familyUpdate(id, data) {
        return request.put(`/elder-families/${id}`, data);
    },
    familyDelete(id) {
        return request.delete(`/elder-families/${id}`);
    },
    //收费项目
    chargeList(elderId, month) {
        return request.get("/elder-charges", {params: {elderId, month}});
    },
    chargeAdd(data) {
        return request.post("/elder-charges", data);
    },
    chargeDelete(id) {
        return request.delete(`/elder-charges/${id}`);
    },
    //月度账单
    bill(elderId, month) {
        return request.get("/elder-charges/bill", {params: {elderId, month}});
    }
}

export default elderProfileApi
