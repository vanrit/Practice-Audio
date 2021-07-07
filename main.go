package main

import (
	"context"
	"encoding/json"
	"fmt"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"net/http"
	"os"
	"reflect"
	"strings"
	"time"
)

const dbName string = "DataBase"
const colName string = "User"

type User struct {
	Login    string   `json:"loginOfUser"`
	Password string   `json:"passwordOfUser"`
	Names    []string `json:"namesOfSongs"`
}

func main() {
	handler := http.NewServeMux()
	handler.HandleFunc("/getInApp/", Logger(authHandler))
	handler.HandleFunc("/audio/", Logger(audioHandler))
	s := http.Server{
		Addr:           ":8080",
		Handler:        handler,
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   10 * time.Second,
		IdleTimeout:    10 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}
	log.Fatal(s.ListenAndServe())
}

func authHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	name := strings.Replace(r.URL.Path, "/getInApp/", "", 1)
	if r.Method == http.MethodPost {
		if name == "registration" || name == "enter" {
			var user User
			decoder := json.NewDecoder(r.Body)
			err := decoder.Decode(&user)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
			} else {
				if !checkFieldsToAuth(user, w) {
					wasError, client := wasErrorInConnectToDb()
					if wasError {
						w.WriteHeader(http.StatusNotFound)
					} else {
						if name == "registration" {
							if isUserInTable(w, r, client, user) {
								http.Error(w, "Already exist", 418)
							} else {
								addToTable(client, user)
								w.WriteHeader(http.StatusOK)
							}
						}
						if name == "enter" {
							if isUserInTable(w, r, client, user) {
								w.WriteHeader(http.StatusOK)
							} else {
								http.Error(w, "Not exist", 418)
							}
						}
					}
				}
			}
		} else {
			http.Error(w, "Wrong path", 418)
		}
	} else {
		w.WriteHeader(http.StatusMethodNotAllowed)
	}
}

func checkFieldsToAuth(user User, w http.ResponseWriter) bool {
	if user.Login == "" {
		http.Error(w, "Wrong login", 418)
		return true
	} else if user.Password == "" {
		http.Error(w, "Wrong password", 418)
		return true
	} else if user.Names != nil {
		http.Error(w, "Wrong names", 418)
		return true
	}
	return false
}

func wasErrorInConnectToDb() (bool, *mongo.Client) {
	wasError := false
	//clientOptions := options.Client().ApplyURI("mongodb://localhost:27017")
	clientOptions := options.Client().
		ApplyURI("mongodb+srv://Polina:Polina1521Misha@cluster0.v80ex.mongodb.net/myFirstDatabase?retryWrites=true&w=majority")
	client, err := mongo.Connect(context.TODO(), clientOptions)
	if err != nil {
		wasError = true
	} else {
		err = client.Ping(context.TODO(), nil)
		if err != nil {
			wasError = true
		}
	}
	log.Printf("Connected to MongoDB!")
	return wasError, client
}

func readFromDb(client *mongo.Client) []User {
	col := client.Database(dbName).Collection(colName)
	filter := bson.D{}
	var users []User
	cur, err := col.Find(context.TODO(), filter, options.Find())
	if err != nil {
		log.Fatal(err)
	}
	for cur.Next(context.TODO()) {
		var elem User
		err := cur.Decode(&elem)
		if err != nil {
			log.Fatal(err)
		}
		users = append(users, elem)
	}
	if err := cur.Err(); err != nil {
		log.Fatal(err)
	}
	cur.Close(context.TODO())
	return users
}

func addToTable(client *mongo.Client, user User) {
	ctx, _ := context.WithTimeout(context.Background(), 15*time.Second)
	col := client.Database(dbName).Collection(colName)
	result, insertErr := col.InsertOne(ctx, user)
	if insertErr != nil {
		log.Printf("InsertOne ERROR:%v", insertErr)
		os.Exit(1)
	} else {
		newID := result.InsertedID
		fmt.Println("InsertedOne(), newID", newID)
		fmt.Println("InsertedOne(), newID type:", reflect.TypeOf(newID))
	}
}

func audioHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	//name := strings.Replace(r.URL.Path, "/audio/", "", 1)
}

func isUserInTable(w http.ResponseWriter, r *http.Request, client *mongo.Client, user User) bool {
	users := readFromDb(client)
	for _, element := range users {
		if element.Login == user.Login && element.Password == user.Password {
			return true
		}
	}
	return false
}

func Logger(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		log.Printf("server [net/http] method [%s] connection from [%v]", r.Method, r.RemoteAddr)
		next.ServeHTTP(w, r)
	}
}
