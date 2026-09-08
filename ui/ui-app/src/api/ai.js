import request from "@/utils/request.js";

//康养小智 AI 健康助手
const aiApi = {
    //history: [{role:'user'|'assistant', content}]
    chat(messages) {
        return request.post("/ai/chat", {messages});
    }
}

export default aiApi
