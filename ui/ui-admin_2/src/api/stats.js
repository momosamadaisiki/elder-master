import request from "@/utils/request.js";

//统计接口（首页看板/大屏数据可视化）
const statsApi = {
    //看板统计概览：老人状态/年龄/护理计划等级分布 + 护理任务近7天趋势
    overview() {
        return request.get("/stats/overview");
    },
    //数据大屏指标（含 overview 全部 + 今日任务/体检预约/异常/探视）
    screen() {
        return request.get("/stats/screen");
    }
}

export default statsApi
