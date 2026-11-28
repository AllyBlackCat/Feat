import { useState, type ChangeEvent } from 'react';
import './Signin.css';

function Signin() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    function handleEmailChange(event: ChangeEvent<HTMLInputElement>) {
        setEmail(event.target.value);
    }

    function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
        setPassword(event.target.value);
    }

    return (
        <main>
            <form>
                <div className="signin-form">
                    <h1>ログイン</h1>
                    <label htmlFor="email">メールアドレス</label>
                    <input 
                        id="email" 
                        type="email" 
                        required
                        value={email}
                        onChange={handleEmailChange} />
                    <label htmlFor="password">パスワード</label>
                    <input 
                        id="password" 
                        type="password" 
                        required 
                        value={password}
                        onChange={handlePasswordChange} />
                    <button type="submit">ログイン</button>
                </div>
            </form>
        </main>
    );
}
export default Signin;