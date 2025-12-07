import axios from "axios";

// φτιάχνουμε ένα axios instance για όλο το app
const http = axios.create({
baseURL: "http://localhost:8080", // Spring Boot
withCredentials: false,
headers: {
"Content-Type": "application/json",
},
});

// helper για να βάζουμε / βγάζουμε το JWT token
export function setAuthToken(token: string | null) {
  if (token) {
    http.defaults.headers.common["Authorization"] = `Bearer token`;
    http.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete http.defaults.headers.common["Authorization"];
  }
}


export default http;


export { http };
