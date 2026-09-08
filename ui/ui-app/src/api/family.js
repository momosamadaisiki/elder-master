import request from "@/utils/request.js";

//家属端接口（只读查看绑定老人健康信息）
const familyApi = {
    login(data) {
        return request.post("/family/login", data);
    },
    //当前家属绑定的老人只读视图
    me() {
        return request.get("/family/me");
    }
}

export default familyApi
