import './Signin.css';

function Signin() {
    return (
        <main>
            <form>
                <div className="signin-form">
                    <h1>ログイン</h1>
                    <label htmlFor="email">メールアドレス</label>
                    <input id="email" type="email" required />
                    <label htmlFor="password">パスワード</label>
                    <input id="password" type="password" required />
                    <button type="submit">ログイン</button>
                </div>
            </form>
        </main>
    );
}
export default Signin;