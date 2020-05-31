package jp.techacademy.kouji.qa_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import android.util.Log

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mLikeRef: DatabaseReference

    private lateinit var mAuth: FirebaseAuth
    private var mGenre: Int = 0
    private lateinit var mLike: Like


    private val mEventListener = object : ChildEventListener {
        @SuppressLint("RestrictedApi")
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>


            val answerUid = dataSnapshot.key ?: ""


            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()

        }


        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }


    }
//お気に入りボタンの追加
    private var mLikeFlag = false //falseはお気に入りしていない、trueはお気に入りしている

    private val mLikeListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            //解除ボタンに切り替える処理を書く





        }


        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }


    }

   //ここまで


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


        fab.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            like_button.setVisibility(View.INVISIBLE)

        } else {
            like_button.setVisibility(View.VISIBLE)

        }

        val extra = intent.extras
        mGenre = extra.getInt("genre")

        //------------------
        like_button.setOnClickListener{v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            //var mGenre: Int = 0
            //mGenre = extras.getInt("genre")
            val user = FirebaseAuth.getInstance().currentUser

            val likeRef = dataBaseReference.child(LikePATH).child(UsersPATH).child(user!!.uid).child(mQuestion.questionUid)

            val data = HashMap<String, String>()

            if (likeRef !== null) {
                val genre = mQuestion.genre
                data["genre"] = genre.toString()
                like_button.setImageResource(R.drawable.like)
                likeRef.setValue(data)

            } else {
                like_button.setImageResource(R.drawable.none)
                likeRef.removeValue()

            }


        }


        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        mLikeRef = dataBaseReference.child(LikePATH).child(UsersPATH).child(user!!.uid).child(mQuestion.questionUid)
        mLikeRef.addChildEventListener(mLikeListener)



    }









}